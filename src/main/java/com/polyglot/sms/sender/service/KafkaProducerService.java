package com.polyglot.sms.sender.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import com.polyglot.sms.sender.entity.FailedKafkaEvent;
import com.polyglot.sms.sender.repository.FailedEventRepository;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.polyglot.sms.sender.dto.SmsEvent;
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    // private final MongoTemplate mongoTemplate;
    private final FailedEventRepository repository;
    private final ObjectMapper objectMapper;

    public void sendMessage(String topic, String key, Object event) {
        try {
            // If Metadata fetch fails (Kafka down), this throws Exception immediately.since max blocking time for producer is set to 2s for fetching metadata
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            // Asynchronous failures when kafka crashes after data being sent to broker
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Async Kafka Failure. Saving to SQL. Key: {}", key, ex);
                    saveToFallback(topic, key, event);
                } else {
                    log.info("Success: Offset {}", result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            // Synchronous failures (Kafka totally down / Metadata timeout)
            log.error("Sync Kafka Failure (Metadata/Timeout). Saving to SQL. Key: {}", key, e);
            saveToFallback(topic, key, event);
        }
    }

    private void saveToFallback(String topic, String key, Object event) {
        try {
            // Convert the object to a JSON String
            String payload = objectMapper.writeValueAsString(event);
            SmsEvent smsEvent = (SmsEvent) event;
            
            FailedKafkaEvent fallback = FailedKafkaEvent.builder()
                    .topic(topic)
                    .eventKey(key)
                    .idempotencyKey(smsEvent.getIdempotencyKey())
                    .eventPayload(payload)
                    .createdAt(System.currentTimeMillis())
                    .build();
            
            repository.save(fallback);
            
            log.info("Saved failed event to SQL fallback for key: {}", key);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event to JSON for key: {}", key, e);
        } catch (Exception e) {
            log.error("CRITICAL: Both Kafka and SQL are DOWN. Data lost for key: {}", key, e);
        }
    }
}
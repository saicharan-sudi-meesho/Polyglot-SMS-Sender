package com.polyglot.sms.sender.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendMessage(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
    
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent message to topic {} at offset {}",topic, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message to Kafka", ex);
                // Optional: Save to a 'FailedEvents' table in DB
            }
        });
    }
}

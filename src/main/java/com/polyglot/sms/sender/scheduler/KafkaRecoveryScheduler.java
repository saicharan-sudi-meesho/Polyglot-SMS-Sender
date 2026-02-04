package com.polyglot.sms.sender.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polyglot.sms.sender.entity.FailedKafkaEvent;
import com.polyglot.sms.sender.repository.FailedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaRecoveryScheduler {

    private final FailedEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper; // to parse JSON String back to Object

    @Scheduled(fixedDelay = 60000)
    public void retryFailedEvents() {
        
        List<FailedKafkaEvent> failures = repository.findTop50Oldest();
        
        if (failures.isEmpty()) {
            return;
        }

        log.info("Kafka Recovery: Processing {} failed events from SQL...", failures.size());

        for (FailedKafkaEvent failure : failures) {
            try {
                //  Converting JSON String back to an Object
                Object eventObject = objectMapper.readValue(failure.getEventPayload(), Object.class);

                // Resend to Kafka and wait for confirmation (.get())
                kafkaTemplate.send(failure.getTopic(), failure.getEventKey(), eventObject).get();

                // If successful, remove from SQL
                repository.delete(failure);
                log.info("Successfully recovered event for key: {}", failure.getEventKey());

            } catch (Exception e) {
                log.error("Recovery failed for key: {}. Stopping batch. Error: {}", failure.getEventKey(), e.getMessage());
                // Break the loop: If Kafka is still down, don't keep trying the rest of the batch
                break;
            }
        }
    }
}
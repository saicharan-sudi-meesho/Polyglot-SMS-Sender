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
import com.polyglot.sms.sender.dto.SmsEvent;
import java.util.concurrent.TimeUnit;
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaRecoveryScheduler {

    private final FailedEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper; // to parse JSON String back to Object

    @Scheduled(fixedDelay = 60000)
    public void retryFailedEvents() {
        
        List<FailedKafkaEvent> failures = repository.claimAndGetTop50Pending();
        
        if (failures.isEmpty()) {
            return;
        }

        log.info("Kafka Recovery: Processing {} failed events from SQL...", failures.size());
        int processedCount = 0;
        for (int i = 0; i < failures.size(); i++) {
            FailedKafkaEvent failure = failures.get(i);
            try {
                // Converting JSON String back to an Object
                SmsEvent eventObject = objectMapper.readValue(failure.getEventPayload(), SmsEvent.class);
                // Using a timeout to prevent hanging
                kafkaTemplate.send(failure.getTopic(), failure.getEventKey(), eventObject).get(15, TimeUnit.SECONDS);
                repository.delete(failure);
                processedCount++;
                log.info("Successfully recovered event for key: {}, SMS Event: {}", failure.getEventKey(), eventObject);
            } catch (Exception e) {
                log.error("Kafka unreachable. Reverting remaining {} records to PENDING.", failures.size() - i);
                for (int j = i; j < failures.size(); j++) {
                    FailedKafkaEvent remaining = failures.get(j);
                    remaining.setStatus("PENDING");
                    remaining.setRetryCount(remaining.getRetryCount() + 1);
                }
                repository.saveAll(failures.subList(i, failures.size()));
                break;
            }
        }
        log.info("Successfully recovered {}/{} events.", processedCount, failures.size());
    }
}
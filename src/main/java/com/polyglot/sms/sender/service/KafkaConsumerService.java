package com.polyglot.sms.sender.service;

import com.polyglot.sms.sender.dto.SmsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "${app.kafka.topic.sms-events}", groupId = "sms-sender-debug-group")
    public void listen(SmsEvent event) {
        log.info("[DEBUG CONSUMER] Received Message from Kafka: {}", event);
        log.info("   -> Status: {}", event.getStatus());
        log.info("   -> User Id: {}", event.getUserId());
    }
}

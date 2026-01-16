package com.polyglot.sms.sender.service;

import org.springframework.stereotype.Service;
import com.polyglot.sms.sender.dto.SmsEvent;
import com.polyglot.sms.sender.dto.SmsRequest;
import com.polyglot.sms.sender.dto.SmsResponse;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RedisService redisService;
    private final KafkaProducerService kafkaProducerService;

    @Value("${app.kafka.topic.sms-events}")
    private String topicName;
    
    public SmsResponse sendSms(SmsRequest request){

        String userId=request.getUserId();
        log.info("Processing SMS request for User: {}", request.getUserId());

        try{
            if(redisService.isBlocked(userId)){
                log.warn("User: {} is blocked. Sending SMS failed.", userId);
                return SmsResponse.builder()
                    .userId(userId)
                    .status("BLOCKED")
                    .message("User is blocked from sending SMS")
                    .build();
            }
        }catch(Exception e){
            log.error("Failed to reach Redis", e);
            return SmsResponse.builder()
                    .userId(userId)
                    .status("INTERNAL_ERROR")
                    .message("Failed to process, Redis Service is down")
                    .build();
        }


        // Call 3rd Party API (Mocked)
        boolean vendorSuccess = callMockThirdPartyApi(request);
        String status = vendorSuccess ? "SUCCESS" : "FAIL";

        SmsEvent event = SmsEvent.builder()
                .userId(request.getUserId())
                .messageContent(request.getMessage())
                .status(status)
                .timestamp(System.currentTimeMillis())
                .build();


        try {
            kafkaProducerService.sendMessage(topicName, userId, event);
            log.info("Event published to Kafka topic '{}': {}", topicName, event);
        } catch (Exception e) {
            log.error("Failed to publish to Kafka", e);
            // Retry Logic to be implemented
            return SmsResponse.builder()
                    .userId(userId)
                    .status("INTERNAL_ERROR")
                    .message("Failed to process request, Kafka is down")
                    .build();
        }



        return SmsResponse.builder()
            .userId(request.getUserId())
            .status(status)
            .message("SMS sent successfully")
            .build();
    }

    // Mocking the 3rd party call
    private boolean callMockThirdPartyApi(SmsRequest request) {
        log.info("Calling 3rd party vendor for {}...", request.getUserId());
        return true;
    }
}

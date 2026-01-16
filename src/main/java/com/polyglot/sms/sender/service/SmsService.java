package com.polyglot.sms.sender.service;

import org.springframework.stereotype.Service;
import com.polyglot.sms.sender.dto.SmsRequest;
import com.polyglot.sms.sender.dto.SmsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RedisService redisService;
    
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


        return SmsResponse.builder()
            .userId(request.getUserId())
            .status("success")
            .message("SMS sent successfully")
            .build();
    }
}

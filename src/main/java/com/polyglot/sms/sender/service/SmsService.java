package com.polyglot.sms.sender.service;

import org.springframework.stereotype.Service;
import com.polyglot.sms.sender.dto.SmsRequest;
import com.polyglot.sms.sender.dto.SmsResponse;

@Service
public class SmsService {
    
    public SmsResponse sendSms(SmsRequest request){
        return SmsResponse.builder()
            .userId(request.getUserId())
            .status("success")
            .message("SMS sent successfully")
            .build();
    }
}

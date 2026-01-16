package com.polyglot.sms.sender.controller;

import com.polyglot.sms.sender.dto.SmsRequest;
import com.polyglot.sms.sender.dto.SmsResponse;
import com.polyglot.sms.sender.service.SmsService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/v1/sms")
@RequiredArgsConstructor
public class SmsController {
    
    private final SmsService smsService;

    @PostMapping("send")
    public ResponseEntity<?> sendSms(@Valid @RequestBody SmsRequest request){
        SmsResponse response = smsService.sendSms(request);
        if ("BLOCKED".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if("INTERNAL_ERROR".equals(response.getStatus())){
            return ResponseEntity.internalServerError()
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
    
}

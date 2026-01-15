package com.polyglot.sms.sender.controller;

import com.polyglot.sms.sender.dto.SmsRequest;
import com.polyglot.sms.sender.service.SmsService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/v1/sms")
@RequiredArgsConstructor
public class SmsController {
    
    private final SmsService smsService;

    @PostMapping("send")
    public ResponseEntity<?> sendSms(@Valid @RequestBody SmsRequest request){
        return ResponseEntity.ok(smsService.sendSms(request));
    }
    
}

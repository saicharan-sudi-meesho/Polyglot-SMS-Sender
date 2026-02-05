package com.polyglot.sms.sender.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor // for Jackson Deserialization
@AllArgsConstructor // for builder
public class SmsEvent {
    private String userId;
    private String messageContent;
    private SmsStatus status;
    private long timestamp;
}

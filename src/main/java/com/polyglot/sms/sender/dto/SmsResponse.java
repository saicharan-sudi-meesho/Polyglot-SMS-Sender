package com.polyglot.sms.sender.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsResponse {
    private String userId;
    private SmsStatus status;// success, blocked, internal_error
    private String message;
}

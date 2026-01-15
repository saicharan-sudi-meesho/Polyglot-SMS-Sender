package com.polyglot.sms.sender.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsResponse {
    public String userId;
    public String status;// success, blocked, internal_error
    public String message;
}

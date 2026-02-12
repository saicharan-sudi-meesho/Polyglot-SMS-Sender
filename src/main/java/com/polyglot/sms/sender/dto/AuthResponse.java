package com.polyglot.sms.sender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class AuthResponse {
    private String status;
    private String message;
    private String error;

    public AuthResponse(String status, String message, String error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }
}
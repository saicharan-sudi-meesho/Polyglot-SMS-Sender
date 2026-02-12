package com.polyglot.sms.sender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsRequest {
    
    @NotBlank(message = "User ID is required")
    @Pattern(regexp = "^\\d{10}$", message = "User ID must be exactly 10 digits")
    private String userId;

    @NotBlank(message = "Message is required")
    private String message;
}

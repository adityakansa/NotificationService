package com.notification.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Value Object representing the result of a notification delivery attempt
 * Encapsulates delivery status and metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResult {
    
    private boolean success;
    private String message;
    private String errorDetails;
    private LocalDateTime timestamp;
    private String channelResponse;
    
    public static DeliveryResult success(String message) {
        return DeliveryResult.builder()
            .success(true)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static DeliveryResult failure(String message, String errorDetails) {
        return DeliveryResult.builder()
            .success(false)
            .message(message)
            .errorDetails(errorDetails)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static DeliveryResult failure(String message, Exception exception) {
        return failure(message, exception.getMessage());
    }
}

package com.notification.dto;

import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for notification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    private Long userId;
    private String subject;
    private String body;
    private NotificationChannel channel;
    private NotificationPriority priority;
    private NotificationStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Integer currentRetryAttempt;
    private Integer maxRetryAttempts;
    private String lastFailureReason;
    private Map<String, String> metadata;
}

package com.notification.dto;

import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.RecurrenceFrequency;
import com.notification.domain.enums.ScheduleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for creating notification requests
 * Follows DTO pattern to separate API layer from domain layer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotBlank(message = "Subject is required")
    @Size(min = 1, max = 200, message = "Subject must be between 1 and 200 characters")
    private String subject;
    
    @NotBlank(message = "Body is required")
    @Size(min = 1, max = 5000, message = "Body must be between 1 and 5000 characters")
    private String body;
    
    @NotNull(message = "Channel is required")
    private NotificationChannel channel;
    
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    
    @Builder.Default
    private ScheduleType scheduleType = ScheduleType.IMMEDIATE;
    
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledTime;
    
    // Recurring notification fields
    private RecurrenceFrequency recurrenceFrequency;
    
    @Future(message = "Recurrence end time must be in the future")
    private LocalDateTime recurrenceEndTime;
    
    @Positive(message = "Max occurrences must be positive")
    @Max(value = 1000, message = "Max occurrences cannot exceed 1000")
    private Integer maxOccurrences;
    
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    
    private String template;
}

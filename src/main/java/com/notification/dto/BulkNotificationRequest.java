package com.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for bulk notification requests
 * Supports batch processing for efficiency
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkNotificationRequest {
    
    @NotNull(message = "User IDs list is required")
    @NotEmpty(message = "User IDs list cannot be empty")
    @Builder.Default
    private List<Long> userIds = new ArrayList<>();
    
    @NotNull(message = "Notification template is required")
    @Valid
    private NotificationRequest template;
    
    @Builder.Default
    private boolean personalizeContent = true;
}

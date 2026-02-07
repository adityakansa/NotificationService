package com.notification.validation;

import com.notification.domain.enums.ScheduleType;
import com.notification.dto.NotificationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Custom validator for notification scheduling
 * Ensures scheduled notifications have valid timing constraints
 */
@Component
public class ScheduledTimeValidator {
    
    /**
     * Validate scheduled notification timing
     */
    public void validateScheduledTime(NotificationRequest request) {
        if (request.getScheduleType() == ScheduleType.SCHEDULED) {
            if (request.getScheduledTime() == null) {
                throw new IllegalArgumentException("Scheduled time is required for SCHEDULED notifications");
            }
            
            if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Scheduled time must be in the future");
            }
        }
    }
    
    /**
     * Validate recurring notification configuration
     */
    public void validateRecurringNotification(NotificationRequest request) {
        if (request.getScheduleType() == ScheduleType.RECURRING) {
            if (request.getRecurrenceFrequency() == null) {
                throw new IllegalArgumentException("Recurrence frequency is required for RECURRING notifications");
            }
            
            if (request.getScheduledTime() == null) {
                throw new IllegalArgumentException("Initial scheduled time is required for RECURRING notifications");
            }
            
            if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Initial scheduled time must be in the future");
            }
            
            // Must have either end time or max occurrences
            if (request.getRecurrenceEndTime() == null && request.getMaxOccurrences() == null) {
                throw new IllegalArgumentException(
                    "Recurring notifications must have either recurrence end time or max occurrences");
            }
            
            // If end time is specified, it must be after scheduled time
            if (request.getRecurrenceEndTime() != null 
                && !request.getRecurrenceEndTime().isAfter(request.getScheduledTime())) {
                throw new IllegalArgumentException("Recurrence end time must be after scheduled time");
            }
            
            // Validate max occurrences is reasonable
            if (request.getMaxOccurrences() != null) {
                if (request.getMaxOccurrences() < 1) {
                    throw new IllegalArgumentException("Max occurrences must be at least 1");
                }
                if (request.getMaxOccurrences() > 1000) {
                    throw new IllegalArgumentException("Max occurrences cannot exceed 1000");
                }
            }
        }
    }
    
    /**
     * Validate reschedule time
     */
    public void validateRescheduleTime(LocalDateTime newTime) {
        if (newTime == null) {
            throw new IllegalArgumentException("New scheduled time is required");
        }
        
        if (newTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New scheduled time must be in the future");
        }
    }
}

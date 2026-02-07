package com.notification.domain.model;

import com.notification.domain.enums.RecurrenceFrequency;
import com.notification.domain.enums.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Value Object for scheduling configuration
 * Encapsulates all scheduling-related logic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConfig {
    
    @Builder.Default
    private ScheduleType scheduleType = ScheduleType.IMMEDIATE;
    
    private LocalDateTime scheduledTime;
    private RecurrenceFrequency recurrenceFrequency;
    private LocalDateTime recurrenceEndTime;
    private Integer maxOccurrences;
    
    /**
     * Check if notification should be sent now
     */
    public boolean shouldSendNow() {
        if (scheduleType == ScheduleType.IMMEDIATE) {
            return true;
        }
        
        if (scheduleType == ScheduleType.SCHEDULED && scheduledTime != null) {
            return LocalDateTime.now().isAfter(scheduledTime) || 
                   LocalDateTime.now().isEqual(scheduledTime);
        }
        
        return false;
    }
    
    /**
     * Check if recurrence is active
     */
    public boolean isRecurring() {
        return scheduleType == ScheduleType.RECURRING && recurrenceFrequency != null;
    }
    
    /**
     * Calculate next execution time for recurring notifications
     */
    public LocalDateTime getNextExecutionTime(LocalDateTime lastExecution) {
        if (!isRecurring() || recurrenceFrequency == null) {
            return null;
        }
        
        LocalDateTime nextTime = lastExecution.plusSeconds(
            recurrenceFrequency.getIntervalMillis() / 1000
        );
        
        if (recurrenceEndTime != null && nextTime.isAfter(recurrenceEndTime)) {
            return null;
        }
        
        return nextTime;
    }
}

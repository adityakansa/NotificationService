package com.notification.domain.entity;

import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import com.notification.domain.enums.ScheduleType;
import com.notification.domain.enums.RecurrenceFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification Entity - Core domain entity
 * Aggregate Root that encapsulates notification lifecycle
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_status", columnList = "user_id, status"),
    @Index(name = "idx_status_priority", columnList = "status, priority"),
    @Index(name = "idx_scheduled_time", columnList = "scheduled_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(nullable = false, length = 2000)
    private String body;
    
    private String template;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleType scheduleType = ScheduleType.IMMEDIATE;
    
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    @Enumerated(EnumType.STRING)
    private RecurrenceFrequency recurrenceFrequency;
    
    @Column(name = "recurrence_end_time")
    private LocalDateTime recurrenceEndTime;
    
    @Column(name = "max_occurrences")
    private Integer maxOccurrences;
    
    @Column(name = "occurrence_count")
    @Builder.Default
    private Integer occurrenceCount = 0;
    
    // Retry configuration
    @Column(name = "max_retry_attempts")
    @Builder.Default
    private Integer maxRetryAttempts = 3;
    
    @Column(name = "current_retry_attempt")
    @Builder.Default
    private Integer currentRetryAttempt = 0;
    
    @Column(name = "next_retry_time")
    private LocalDateTime nextRetryTime;
    
    @Column(name = "last_failure_reason", length = 500)
    private String lastFailureReason;
    
    // Metadata for additional information
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_metadata", 
                     joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    
    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark notification as sent
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.currentRetryAttempt = 0;
        this.lastFailureReason = null;
    }
    
    /**
     * Mark notification as failed with reason
     */
    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.lastFailureReason = reason;
    }
    
    /**
     * Record a retry attempt
     */
    public void recordRetryAttempt(String failureReason) {
        this.currentRetryAttempt++;
        this.lastFailureReason = failureReason;
        this.status = NotificationStatus.RETRY;
        
        if (canRetry()) {
            this.nextRetryTime = calculateNextRetryTime();
        } else {
            markAsFailed("Max retry attempts exceeded: " + failureReason);
        }
    }
    
    /**
     * Check if retry is possible
     */
    public boolean canRetry() {
        return currentRetryAttempt < maxRetryAttempts;
    }
    
    /**
     * Calculate next retry time with exponential backoff
     */
    private LocalDateTime calculateNextRetryTime() {
        long baseDelay = 1000L; // 1 second
        long delay = (long) (baseDelay * Math.pow(2, currentRetryAttempt));
        long maxDelay = 10000L; // 10 seconds
        delay = Math.min(delay, maxDelay);
        
        return LocalDateTime.now().plusSeconds(delay / 1000);
    }
    
    /**
     * Increment occurrence count for recurring notifications
     */
    public void incrementOccurrence() {
        this.occurrenceCount++;
    }
    
    /**
     * Check if recurring notification should continue
     */
    public boolean shouldContinueRecurrence() {
        if (scheduleType != ScheduleType.RECURRING) {
            return false;
        }
        
        if (maxOccurrences != null && occurrenceCount >= maxOccurrences) {
            return false;
        }
        
        if (recurrenceEndTime != null && LocalDateTime.now().isAfter(recurrenceEndTime)) {
            return false;
        }
        
        return true;
    }
}

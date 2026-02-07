package com.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Value Object for retry configuration and tracking
 * Implements exponential backoff strategy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryConfig {
    
    @Builder.Default
    private int maxAttempts = 3;
    
    @Builder.Default
    private int currentAttempt = 0;
    
    @Builder.Default
    private long initialIntervalMillis = 1000L; // 1 second
    
    @Builder.Default
    private double backoffMultiplier = 2.0;
    
    @Builder.Default
    private long maxIntervalMillis = 10000L; // 10 seconds
    
    private LocalDateTime lastAttemptTime;
    private String lastFailureReason;
    
    /**
     * Check if retry is allowed
     */
    public boolean canRetry() {
        return currentAttempt < maxAttempts;
    }
    
    /**
     * Calculate next retry time using exponential backoff
     */
    public LocalDateTime getNextRetryTime() {
        if (!canRetry()) {
            return null;
        }
        
        long delayMillis = calculateBackoffDelay();
        return LocalDateTime.now().plusSeconds(delayMillis / 1000);
    }
    
    /**
     * Calculate backoff delay with exponential increase
     */
    private long calculateBackoffDelay() {
        long delay = (long) (initialIntervalMillis * Math.pow(backoffMultiplier, currentAttempt));
        return Math.min(delay, maxIntervalMillis);
    }
    
    /**
     * Record a failed attempt
     */
    public void recordFailure(String reason) {
        this.currentAttempt++;
        this.lastAttemptTime = LocalDateTime.now();
        this.lastFailureReason = reason;
    }
    
    /**
     * Reset retry configuration
     */
    public void reset() {
        this.currentAttempt = 0;
        this.lastAttemptTime = null;
        this.lastFailureReason = null;
    }
}

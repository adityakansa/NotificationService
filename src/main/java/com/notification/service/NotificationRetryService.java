package com.notification.service;

import com.notification.domain.entity.Notification;
import com.notification.domain.entity.NotificationHistory;
import com.notification.domain.enums.NotificationStatus;
import com.notification.repository.NotificationHistoryRepository;
import com.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Retry Service for handling failed notifications
 * Implements exponential backoff strategy
 * Follows Single Responsibility Principle - manages only retry logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRetryService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationHistoryRepository historyRepository;
    private final NotificationService notificationService;
    
    @Value("${notification.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${notification.retry.initial-interval:1000}")
    private long initialRetryInterval;
    
    @Value("${notification.retry.multiplier:2.0}")
    private double retryMultiplier;
    
    @Value("${notification.retry.max-interval:10000}")
    private long maxRetryInterval;
    
    /**
     * Process notifications that are due for retry
     * Runs every minute
     */
    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    @Transactional
    public void processRetryNotifications() {
        log.debug("Checking for notifications to retry...");
        
        List<Notification> retryNotifications = notificationRepository.findNotificationsForRetry(
            NotificationStatus.RETRY,
            LocalDateTime.now()
        );
        
        if (!retryNotifications.isEmpty()) {
            log.info("Found {} notifications ready for retry", retryNotifications.size());
            
            for (Notification notification : retryNotifications) {
                try {
                    retryNotification(notification);
                } catch (Exception e) {
                    log.error("Failed to retry notification {}: {}", 
                             notification.getId(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Retry a specific notification
     */
    @Transactional
    public void retryNotification(Notification notification) {
        log.info("Retrying notification {} (attempt {}/{})", 
                 notification.getId(), 
                 notification.getCurrentRetryAttempt() + 1,
                 notification.getMaxRetryAttempts());
        
        try {
            // Attempt to send
            notificationService.sendNotification(notification.getId());
            
            log.info("Retry successful for notification {}", notification.getId());
            
            // Create history entry for successful retry
            createRetryHistoryEntry(
                notification,
                "Retry successful",
                null
            );
            
        } catch (Exception e) {
            log.warn("Retry failed for notification {}: {}", 
                     notification.getId(), e.getMessage());
            
            // Record failure and schedule next retry if possible
            notification.recordRetryAttempt(e.getMessage());
            notificationRepository.save(notification);
            
            // Create history entry for failed retry
            createRetryHistoryEntry(
                notification,
                "Retry failed",
                e.getMessage()
            );
            
            if (!notification.canRetry()) {
                log.error("Max retry attempts reached for notification {}. Marking as permanently failed.", 
                         notification.getId());
                
                notification.markAsFailed("Max retry attempts exceeded: " + e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }
    
    /**
     * Calculate exponential backoff delay
     */
    public long calculateBackoffDelay(int attemptNumber) {
        long delay = (long) (initialRetryInterval * Math.pow(retryMultiplier, attemptNumber));
        return Math.min(delay, maxRetryInterval);
    }
    
    /**
     * Get retry statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRetryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count notifications in retry state
        long retryCount = notificationRepository.countByStatus(NotificationStatus.RETRY);
        stats.put("currentRetryCount", retryCount);
        
        // Count permanently failed
        long failedCount = notificationRepository.countByStatus(NotificationStatus.FAILED);
        stats.put("permanentlyFailedCount", failedCount);
        
        // Retry configuration
        Map<String, Object> config = new HashMap<>();
        config.put("maxAttempts", maxRetryAttempts);
        config.put("initialInterval", initialRetryInterval);
        config.put("multiplier", retryMultiplier);
        config.put("maxInterval", maxRetryInterval);
        stats.put("configuration", config);
        
        // Recent retry history
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long recentRetries = historyRepository.countByStatusSince(
            NotificationStatus.RETRY, 
            last24Hours
        );
        stats.put("retriesLast24Hours", recentRetries);
        
        return stats;
    }
    
    /**
     * Manually trigger retry for a specific notification
     */
    @Transactional
    public void manualRetry(Long notificationId) {
        log.info("Manual retry requested for notification: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new IllegalStateException("Cannot retry a notification that was already sent successfully");
        }
        
        // Reset retry counter for manual retry
        notification.setCurrentRetryAttempt(0);
        notification.setStatus(NotificationStatus.RETRY);
        notification.setNextRetryTime(LocalDateTime.now());
        notificationRepository.save(notification);
        
        // Trigger immediate retry
        retryNotification(notification);
    }
    
    /**
     * Reset notification for retry
     */
    @Transactional
    public void resetForRetry(Long notificationId) {
        log.info("Resetting notification {} for retry", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        notification.setCurrentRetryAttempt(0);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setNextRetryTime(null);
        notification.setLastFailureReason(null);
        
        notificationRepository.save(notification);
        
        log.info("Notification {} reset successfully", notificationId);
    }
    
    // Private helper methods
    
    private void createRetryHistoryEntry(Notification notification, String message, String errorDetails) {
        NotificationHistory history = NotificationHistory.builder()
            .notificationId(notification.getId())
            .status(notification.getStatus())
            .attemptNumber(notification.getCurrentRetryAttempt())
            .message(message)
            .errorDetails(errorDetails)
            .build();
        
        historyRepository.save(history);
    }
}

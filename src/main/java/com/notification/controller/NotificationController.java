package com.notification.controller;

import com.notification.domain.entity.NotificationHistory;
import com.notification.dto.BulkNotificationRequest;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.service.NotificationBatchService;
import com.notification.service.NotificationRetryService;
import com.notification.service.NotificationSchedulerService;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Notification Operations
 * Provides comprehensive APIs for notification management
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final NotificationSchedulerService schedulerService;
    private final NotificationBatchService batchService;
    private final NotificationRetryService retryService;
    
    /**
     * Create a single notification
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        log.info("REST API: Create notification for user {}", request.getUserId());
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Create bulk notifications
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<NotificationResponse>> createBulkNotifications(
            @Valid @RequestBody BulkNotificationRequest request) {
        log.info("REST API: Create bulk notifications for {} users", request.getUserIds().size());
        List<NotificationResponse> responses = notificationService.createBulkNotifications(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    /**
     * Send a notification immediately
     */
    @PostMapping("/{notificationId}/send")
    public ResponseEntity<NotificationResponse> sendNotification(@PathVariable Long notificationId) {
        log.info("REST API: Send notification {}", notificationId);
        NotificationResponse response = notificationService.sendNotification(notificationId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all notifications (for tracking)
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        log.info("REST API: Get all notifications");
        List<NotificationResponse> responses = notificationService.getAllNotifications();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get notification by ID
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long notificationId) {
        log.info("REST API: Get notification {}", notificationId);
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all notifications for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable Long userId) {
        log.info("REST API: Get notifications for user {}", userId);
        List<NotificationResponse> responses = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get notification history
     */
    @GetMapping("/{notificationId}/history")
    public ResponseEntity<List<NotificationHistory>> getNotificationHistory(
            @PathVariable Long notificationId) {
        log.info("REST API: Get history for notification {}", notificationId);
        List<NotificationHistory> history = notificationService.getNotificationHistory(notificationId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Cancel a scheduled notification
     */
    @DeleteMapping("/{notificationId}/schedule")
    public ResponseEntity<NotificationResponse> cancelScheduledNotification(@PathVariable Long notificationId) {
        log.info("REST API: Cancel scheduled notification {}", notificationId);
        schedulerService.cancelScheduledNotification(notificationId);
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(response);
    }
    

    /**
     * Reschedule a notification
     */
    @PutMapping("/{notificationId}/schedule")
    public ResponseEntity<NotificationResponse> rescheduleNotification(
            @PathVariable Long notificationId,
            @RequestBody Map<String, String> payload) {
        if (payload == null || !payload.containsKey("scheduledTime") || payload.get("scheduledTime") == null) {
            throw new IllegalArgumentException("scheduledTime is required in request body");
        }
        LocalDateTime newTime = LocalDateTime.parse(payload.get("scheduledTime"));
        log.info("REST API: Reschedule notification {} to {}", notificationId, newTime);
        schedulerService.rescheduleNotification(notificationId, newTime);
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Manually retry a failed notification
     */
    @PostMapping("/{notificationId}/retry")
    public ResponseEntity<NotificationResponse> retryNotification(@PathVariable Long notificationId) {
        log.info("REST API: Manual retry for notification {}", notificationId);
        retryService.manualRetry(notificationId);
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reset notification for retry
     */
    @PostMapping("/{notificationId}/reset")
    public ResponseEntity<NotificationResponse> resetNotification(@PathVariable Long notificationId) {
        log.info("REST API: Reset notification {}", notificationId);
        retryService.resetForRetry(notificationId);
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Process pending notifications manually
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Integer>> processPendingNotifications() {
        log.info("REST API: Process pending notifications");
        int processed = notificationService.processPendingNotifications();
        return ResponseEntity.ok(Map.of("processedCount", processed));
    }
    
    /**
     * Get batch statistics
     */
    @GetMapping("/statistics/batch")
    public ResponseEntity<Map<String, Object>> getBatchStatistics() {
        log.info("REST API: Get batch statistics");
        Map<String, Object> stats = batchService.getBatchStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get retry statistics
     */
    @GetMapping("/statistics/retry")
    public ResponseEntity<Map<String, Object>> getRetryStatistics() {
        log.info("REST API: Get retry statistics");
        Map<String, Object> stats = retryService.getRetryStatistics();
        return ResponseEntity.ok(stats);
    }
}

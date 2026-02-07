package com.notification.service;

import com.notification.channel.DeliveryResult;
import com.notification.channel.NotificationChannelFactory;
import com.notification.channel.NotificationChannelStrategy;
import com.notification.domain.entity.Notification;
import com.notification.domain.entity.NotificationHistory;
import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationStatus;
import com.notification.dto.BulkNotificationRequest;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.repository.NotificationHistoryRepository;
import com.notification.repository.NotificationRepository;
import com.notification.repository.UserRepository;
import com.notification.validation.ScheduledTimeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core Notification Service
 * Follows Single Responsibility Principle - manages notification lifecycle
 * Orchestrates notification creation, validation, and delivery
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationHistoryRepository historyRepository;
    private final NotificationChannelFactory channelFactory;
    private final ScheduledTimeValidator scheduledTimeValidator;
    
    /**
     * Create and queue a notification
     * Follows Command pattern for notification creation
     */
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for user: {}, channel: {}", request.getUserId(), request.getChannel());
        
        // Validate scheduled time constraints
        scheduledTimeValidator.validateScheduledTime(request);
        scheduledTimeValidator.validateRecurringNotification(request);
        
        // Validate user exists
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));
        
        // Validate user can receive on this channel
        if (!user.canReceiveOnChannel(request.getChannel())) {
            throw new IllegalArgumentException(
                String.format("User %s cannot receive notifications on channel %s", 
                              user.getUsername(), request.getChannel())
            );
        }
        
        // Create notification entity
        Notification notification = buildNotificationFromRequest(request, user);
        notification = notificationRepository.save(notification);
        
        log.info("Notification created with ID: {}, status: {}", notification.getId(), notification.getStatus());
        
        // Create history entry
        createHistoryEntry(notification, "Notification created", null);
        
        return mapToResponse(notification);
    }
    
    /**
     * Send notification immediately
     * Delegates to appropriate channel strategy
     */
    @Transactional
    public NotificationResponse sendNotification(Long notificationId) {
        log.info("Sending notification: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        User user = notification.getUser();
        
        // Check if user is active before sending
        if (user.getActive() == null || !user.getActive()) {
            String errorMsg = "User is not active. Cannot send notification.";
            log.warn("Notification {} - {}", notificationId, errorMsg);
            notification.markAsFailed(errorMsg);
            createHistoryEntry(notification, "Send failed", errorMsg);
            notificationRepository.save(notification);
            return mapToResponse(notification);
        }
        
        // Get appropriate channel strategy
        NotificationChannelStrategy channelStrategy = channelFactory.getStrategy(notification.getChannel());
        
        // Update status to processing
        notification.setStatus(NotificationStatus.PROCESSING);
        notificationRepository.save(notification);
        
        // Send notification
        DeliveryResult result = channelStrategy.send(notification, user);
        
        // Update notification based on result
        if (result.isSuccess()) {
            notification.markAsSent();
            createHistoryEntry(notification, result.getMessage(), null);
            log.info("Notification {} sent successfully", notificationId);
        } else {
            notification.recordRetryAttempt(result.getErrorDetails());
            createHistoryEntry(notification, result.getMessage(), result.getErrorDetails());
            log.warn("Notification {} failed: {}", notificationId, result.getErrorDetails());
        }
        
        notification = notificationRepository.save(notification);
        return mapToResponse(notification);
    }
    
    /**
     * Create multiple notifications at once
     * Supports bulk operations for efficiency
     */
    @Transactional
    public List<NotificationResponse> createBulkNotifications(BulkNotificationRequest bulkRequest) {
        log.info("Creating bulk notifications for {} users", bulkRequest.getUserIds().size());
        
        List<NotificationResponse> responses = new ArrayList<>();
        
        for (Long userId : bulkRequest.getUserIds()) {
            try {
                NotificationRequest request = bulkRequest.getTemplate();
                request.setUserId(userId);
                
                NotificationResponse response = createNotification(request);
                responses.add(response);
                
            } catch (Exception e) {
                log.error("Failed to create notification for user {}: {}", userId, e.getMessage());
                // Continue with other users
            }
        }
        
        log.info("Created {} notifications out of {} users", responses.size(), bulkRequest.getUserIds().size());
        return responses;
    }
    
    /**
     * Process pending notifications
     * Called by scheduler or manual trigger
     */
    @Transactional
    public int processPendingNotifications() {
        log.info("Processing pending notifications...");
        
        List<Notification> pendingNotifications = notificationRepository
            .findByStatusOrderByPriorityAndCreatedAt(NotificationStatus.PENDING);
        
        int processedCount = 0;
        
        for (Notification notification : pendingNotifications) {
            try {
                sendNotification(notification.getId());
                processedCount++;
            } catch (Exception e) {
                log.error("Failed to process notification {}: {}", notification.getId(), e.getMessage());
            }
        }
        
        log.info("Processed {} pending notifications", processedCount);
        return processedCount;
    }
    
    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        // Eagerly fetch the user to avoid lazy loading issues
        notification.getUser().getId();
        
        return mapToResponse(notification);
    }
    
    /**
     * Get all notifications for a user (all statuses)
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(
            userId, 
            org.springframework.data.domain.Pageable.unpaged()
        ).getContent();
        
        return notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all notifications (for monitoring/tracking)
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get notification history
     */
    @Transactional(readOnly = true)
    public List<NotificationHistory> getNotificationHistory(Long notificationId) {
        return historyRepository.findByNotificationIdOrderByCreatedAtDesc(notificationId);
    }
    
    // Private helper methods
    
    private Notification buildNotificationFromRequest(NotificationRequest request, User user) {
        // Determine initial status based on schedule type
        NotificationStatus initialStatus;
        if (request.getScheduleType() == com.notification.domain.enums.ScheduleType.SCHEDULED) {
            initialStatus = NotificationStatus.SCHEDULED;
        } else if (request.getScheduleType() == com.notification.domain.enums.ScheduleType.RECURRING) {
            initialStatus = NotificationStatus.SCHEDULED;
        } else {
            initialStatus = NotificationStatus.PENDING;
        }
        
        return Notification.builder()
            .user(user)
            .subject(request.getSubject())
            .body(request.getBody())
            .template(request.getTemplate())
            .channel(request.getChannel())
            .priority(request.getPriority())
            .status(initialStatus)
            .scheduleType(request.getScheduleType())
            .scheduledTime(request.getScheduledTime())
            .recurrenceFrequency(request.getRecurrenceFrequency())
            .recurrenceEndTime(request.getRecurrenceEndTime())
            .maxOccurrences(request.getMaxOccurrences())
            .metadata(request.getMetadata())
            .build();
    }
    
    private void createHistoryEntry(Notification notification, String message, String errorDetails) {
        NotificationHistory history = NotificationHistory.builder()
            .notificationId(notification.getId())
            .status(notification.getStatus())
            .attemptNumber(notification.getCurrentRetryAttempt())
            .message(message)
            .errorDetails(errorDetails)
            .build();
        
        historyRepository.save(history);
    }
    
    private NotificationResponse mapToResponse(Notification notification) {
        Long userId = null;
        if (notification.getUser() != null) {
            userId = notification.getUser().getId();
        }
        
        return NotificationResponse.builder()
            .id(notification.getId())
            .userId(userId)
            .subject(notification.getSubject())
            .body(notification.getBody())
            .channel(notification.getChannel())
            .priority(notification.getPriority())
            .status(notification.getStatus())
            .scheduledTime(notification.getScheduledTime())
            .createdAt(notification.getCreatedAt())
            .sentAt(notification.getSentAt())
            .currentRetryAttempt(notification.getCurrentRetryAttempt())
            .maxRetryAttempts(notification.getMaxRetryAttempts())
            .lastFailureReason(notification.getLastFailureReason())
            .metadata(notification.getMetadata())
            .build();
    }
}

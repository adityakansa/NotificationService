package com.notification.service;

import com.notification.domain.entity.Notification;
import com.notification.domain.enums.NotificationStatus;
import com.notification.domain.enums.ScheduleType;
import com.notification.repository.NotificationRepository;
import com.notification.validation.ScheduledTimeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Notification Scheduler Service
 * Handles scheduled and recurring notifications
 * Follows Single Responsibility Principle - manages only scheduling logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ScheduledTimeValidator scheduledTimeValidator;
    
    /**
     * Process scheduled notifications that are due
     * Runs every minute
     */
    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    public void processScheduledNotifications() {
        log.debug("Checking for scheduled notifications...");
        
        List<Notification> dueNotifications = notificationRepository.findScheduledNotificationsDue(
            ScheduleType.SCHEDULED,
            LocalDateTime.now(),
            NotificationStatus.SCHEDULED
        );
        
        if (!dueNotifications.isEmpty()) {
            log.info("Found {} scheduled notifications due for delivery", dueNotifications.size());
            
            for (Notification notification : dueNotifications) {
                try {
                    log.info("Processing scheduled notification {} (scheduled for: {})", 
                             notification.getId(), notification.getScheduledTime());
                    
                    // Check if user is still active before attempting to send
                    if (notification.getUser().getActive() == null || !notification.getUser().getActive()) {
                        log.warn("Skipping notification {} - User {} is not active", 
                                 notification.getId(), notification.getUser().getId());
                        notification.markAsFailed("User is not active at scheduled send time");
                        notificationRepository.save(notification);
                        continue;
                    }
                    
                    // Send the notification
                    notificationService.sendNotification(notification.getId());
                    
                    log.info("Scheduled notification {} sent successfully", notification.getId());
                } catch (Exception e) {
                    log.error("Failed to send scheduled notification {}: {}", 
                             notification.getId(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Process recurring notifications
     * Runs every minute to support MINUTELY frequency
     */
    @Scheduled(fixedDelay = 60000) // Run every 60 seconds
    public void processRecurringNotifications() {
        log.debug("Checking for recurring notifications...");
        
        List<Notification> recurringNotifications = notificationRepository
            .findRecurringNotificationsForNextExecution();
        
        if (!recurringNotifications.isEmpty()) {
            log.info("Found {} recurring notifications to process", recurringNotifications.size());
            
            for (Notification original : recurringNotifications) {
                try {
                    if (original.shouldContinueRecurrence()) {
                        // Create a new notification instance for the CURRENT occurrence (scheduled now/past)
                        Notification currentOccurrence = Notification.builder()
                            .user(original.getUser())
                            .subject(original.getSubject())
                            .body(original.getBody())
                            .template(original.getTemplate())
                            .channel(original.getChannel())
                            .priority(original.getPriority())
                            .status(NotificationStatus.SCHEDULED)
                            .scheduleType(ScheduleType.SCHEDULED)
                            .scheduledTime(original.getScheduledTime())
                            .metadata(original.getMetadata() != null ? new HashMap<>(original.getMetadata()) : new HashMap<>())
                            .build();
                        
                        notificationRepository.save(currentOccurrence);
                        
                        // Calculate and update to the NEXT scheduled time
                        LocalDateTime nextTime = calculateNextScheduledTime(original);
                        
                        // Validate the next occurrence time is not past recurrence end time
                        if (original.getRecurrenceEndTime() != null && nextTime.isAfter(original.getRecurrenceEndTime())) {
                            log.info("Recurring notification {} has reached its end time", original.getId());
                            continue;
                        }
                        
                        // Update occurrence count and scheduled time for next iteration
                        original.incrementOccurrence();
                        original.setScheduledTime(nextTime);
                        notificationRepository.save(original);
                        
                        log.info("Created occurrence {} of recurring notification {} (scheduled: {}), next occurrence: {}", 
                                 original.getOccurrenceCount(), original.getId(), currentOccurrence.getScheduledTime(), nextTime);
                    } else {
                        log.info("Recurring notification {} has reached its limit", original.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process recurring notification {}: {}", 
                             original.getId(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Schedule a notification for future delivery
     */
    @Transactional
    public Notification scheduleNotification(Notification notification) {
        log.info("Scheduling notification for {}", notification.getScheduledTime());
        
        notification.setStatus(NotificationStatus.SCHEDULED);
        notification = notificationRepository.save(notification);
        
        log.info("Notification {} scheduled successfully", notification.getId());
        return notification;
    }
    
    /**
     * Cancel a scheduled notification
     */
    @Transactional
    public void cancelScheduledNotification(Long notificationId) {
        log.info("Cancelling scheduled notification: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        if (notification.getStatus() != NotificationStatus.SCHEDULED) {
            throw new IllegalStateException("Notification is not in scheduled state");
        }
        
        notification.setStatus(NotificationStatus.CANCELLED);
        notificationRepository.save(notification);
        
        log.info("Notification {} cancelled successfully", notificationId);
    }
    
    /**
     * Reschedule a notification
     */
    @Transactional
    public Notification rescheduleNotification(Long notificationId, LocalDateTime newScheduledTime) {
        log.info("Rescheduling notification {} to {}", notificationId, newScheduledTime);
        
        // Validate new scheduled time
        scheduledTimeValidator.validateRescheduleTime(newScheduledTime);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        
        // Restrict rescheduling of already sent, failed, or cancelled notifications
        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new IllegalStateException("Cannot reschedule notification that has already been sent");
        }
        
        if (notification.getStatus() == NotificationStatus.FAILED) {
            throw new IllegalStateException("Cannot reschedule failed notification. Please reset it first");
        }
        
        if (notification.getStatus() == NotificationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reschedule cancelled notification");
        }
        
        if (notification.getStatus() == NotificationStatus.PROCESSING) {
            throw new IllegalStateException("Cannot reschedule notification that is currently being processed");
        }
        
        notification.setScheduledTime(newScheduledTime);
        notification.setStatus(NotificationStatus.SCHEDULED);
        notification = notificationRepository.save(notification);
        
        log.info("Notification {} rescheduled successfully", notificationId);
        return notification;
    }
    
    // Private helper methods
    
    private LocalDateTime calculateNextScheduledTime(Notification original) {
        if (original.getRecurrenceFrequency() != null && original.getScheduledTime() != null) {
            // Calculate next occurrence from the current scheduled time
            long intervalMillis = original.getRecurrenceFrequency().getIntervalMillis();
            return original.getScheduledTime().plusSeconds(intervalMillis / 1000);
        } else {
            // Fallback to now + interval
            return LocalDateTime.now().plusHours(1);
        }
    }
}

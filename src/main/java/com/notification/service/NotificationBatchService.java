package com.notification.service;

import com.notification.domain.entity.Notification;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Batch Processing Service
 * Handles batching of notifications for efficient delivery
 * Follows Single Responsibility Principle - manages only batch processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationBatchService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    
    @Value("${notification.batch.size:100}")
    private int batchSize;
    
    /**
     * Process notifications in batches based on priority
     * High priority notifications are processed first
     * Runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    @Transactional
    public void processBatchNotifications() {
        log.debug("Starting batch processing of notifications...");
        
        // Get pending notifications ordered by priority
        List<Notification> pendingNotifications = notificationRepository
            .findByStatusInOrderByPriority(
                Arrays.asList(NotificationStatus.PENDING, NotificationStatus.RETRY)
            );
        
        if (pendingNotifications.isEmpty()) {
            log.debug("No pending notifications to process");
            return;
        }
        
        log.info("Found {} notifications to process in batches", pendingNotifications.size());
        
        // Group by priority
        Map<NotificationPriority, List<Notification>> priorityGroups = 
            pendingNotifications.stream()
                .collect(Collectors.groupingBy(Notification::getPriority));
        
        int totalProcessed = 0;
        
        // Process HIGH priority first
        totalProcessed += processPriorityBatch(
            priorityGroups.getOrDefault(NotificationPriority.HIGH, Collections.emptyList()),
            "HIGH"
        );
        
        // Process MEDIUM priority
        totalProcessed += processPriorityBatch(
            priorityGroups.getOrDefault(NotificationPriority.MEDIUM, Collections.emptyList()),
            "MEDIUM"
        );
        
        // Process LOW priority
        totalProcessed += processPriorityBatch(
            priorityGroups.getOrDefault(NotificationPriority.LOW, Collections.emptyList()),
            "LOW"
        );
        
        log.info("Batch processing completed. Processed {} notifications", totalProcessed);
    }
    
    /**
     * Process a batch of notifications for a specific priority level
     */
    private int processPriorityBatch(List<Notification> notifications, String priorityName) {
        if (notifications.isEmpty()) {
            return 0;
        }
        
        log.info("Processing {} {} priority notifications", notifications.size(), priorityName);
        
        int processed = 0;
        int batchCount = 0;
        
        // Process in batches
        for (int i = 0; i < notifications.size(); i += batchSize) {
            batchCount++;
            int endIndex = Math.min(i + batchSize, notifications.size());
            List<Notification> batch = notifications.subList(i, endIndex);
            
            log.info("Processing {} priority batch {}: {} notifications", 
                     priorityName, batchCount, batch.size());
            
            for (Notification notification : batch) {
                try {
                    notificationService.sendNotification(notification.getId());
                    processed++;
                } catch (Exception e) {
                    log.error("Failed to send notification {} in batch: {}", 
                             notification.getId(), e.getMessage());
                }
            }
            
            log.info("Completed {} priority batch {}: {}/{} successful", 
                     priorityName, batchCount, processed, batch.size());
        }
        
        return processed;
    }
    
    /**
     * Get batch statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getBatchStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("batchSize", batchSize);
        stats.put("pendingCount", notificationRepository.countByStatus(NotificationStatus.PENDING));
        stats.put("processingCount", notificationRepository.countByStatus(NotificationStatus.PROCESSING));
        stats.put("sentCount", notificationRepository.countByStatus(NotificationStatus.SENT));
        stats.put("failedCount", notificationRepository.countByStatus(NotificationStatus.FAILED));
        stats.put("retryCount", notificationRepository.countByStatus(NotificationStatus.RETRY));
        
        // Priority breakdown
        Map<String, Long> priorityBreakdown = new HashMap<>();
        for (NotificationPriority priority : NotificationPriority.values()) {
            long count = notificationRepository.findByStatusAndPriority(
                NotificationStatus.PENDING, priority
            ).size();
            priorityBreakdown.put(priority.name(), count);
        }
        stats.put("priorityBreakdown", priorityBreakdown);
        
        return stats;
    }
}

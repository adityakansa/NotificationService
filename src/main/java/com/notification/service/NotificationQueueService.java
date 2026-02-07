package com.notification.service;

import com.notification.domain.entity.Notification;
import com.notification.domain.enums.NotificationPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Priority Queue Service for Notifications
 * Implements a priority-based queue where HIGH priority notifications
 * are processed before MEDIUM and LOW priority ones
 * Follows Single Responsibility Principle - manages only notification queuing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueueService {
    
    private final NotificationService notificationService;
    
    // Priority queue: HIGH > MEDIUM > LOW, then by creation time (FIFO within same priority)
    private final BlockingQueue<QueuedNotification> notificationQueue = 
        new PriorityBlockingQueue<>(100, 
            Comparator.comparing(QueuedNotification::getPriority)
                .thenComparing(QueuedNotification::getQueuedTime));
    
    /**
     * Add notification to priority queue
     */
    public void enqueue(Long notificationId, NotificationPriority priority) {
        QueuedNotification queuedNotification = new QueuedNotification(
            notificationId, 
            priority, 
            System.currentTimeMillis()
        );
        
        boolean added = notificationQueue.offer(queuedNotification);
        if (added) {
            log.info("📥 Queued {} priority notification ID: {} (Queue size: {})", 
                     priority, notificationId, notificationQueue.size());
        } else {
            log.error("❌ Failed to queue notification ID: {}", notificationId);
        }
    }
    
    /**
     * Process all notifications in priority order
     */
    public void processQueue() {
        if (notificationQueue.isEmpty()) {
            log.debug("Queue is empty, nothing to process");
            return;
        }
        
        log.info("\n🚀 Processing notification queue ({} items)...\n", notificationQueue.size());
        
        int processed = 0;
        int failed = 0;
        
        while (!notificationQueue.isEmpty()) {
            QueuedNotification queuedNotification = notificationQueue.poll();
            if (queuedNotification != null) {
                try {
                    log.info("▶️  Processing {} priority notification ID: {}", 
                             queuedNotification.getPriority(), 
                             queuedNotification.getNotificationId());
                    
                    notificationService.sendNotification(queuedNotification.getNotificationId());
                    processed++;
                    
                    log.info("✅ Completed {} priority notification ID: {}\n", 
                             queuedNotification.getPriority(), 
                             queuedNotification.getNotificationId());
                    
                } catch (Exception e) {
                    log.error("❌ Failed to process notification {}: {}\n", 
                             queuedNotification.getNotificationId(), e.getMessage());
                    failed++;
                }
            }
        }
        
        log.info("📊 Queue Processing Summary:");
        log.info("   ✅ Successfully processed: {}", processed);
        if (failed > 0) {
            log.info("   ❌ Failed: {}", failed);
        }
        log.info("   📭 Queue is now empty\n");
    }
    
    /**
     * Get current queue size
     */
    public int getQueueSize() {
        return notificationQueue.size();
    }
    
    /**
     * Clear the queue
     */
    public void clearQueue() {
        notificationQueue.clear();
        log.info("Queue cleared");
    }
    
    /**
     * Internal class to represent a queued notification with priority
     */
    private static class QueuedNotification {
        private final Long notificationId;
        private final NotificationPriority priority;
        private final long queuedTime;
        
        public QueuedNotification(Long notificationId, NotificationPriority priority, long queuedTime) {
            this.notificationId = notificationId;
            this.priority = priority;
            this.queuedTime = queuedTime;
        }
        
        public Long getNotificationId() {
            return notificationId;
        }
        
        public NotificationPriority getPriority() {
            return priority;
        }
        
        public long getQueuedTime() {
            return queuedTime;
        }
    }
}

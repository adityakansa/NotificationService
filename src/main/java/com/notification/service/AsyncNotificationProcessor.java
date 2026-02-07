package com.notification.service;

import com.notification.domain.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Async Notification Processor
 * Handles asynchronous notification processing for real-time notifications
 * Follows Single Responsibility Principle - manages async execution
 */
@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
public class AsyncNotificationProcessor {
    
    private final NotificationQueueService queueService;
    private final NotificationService notificationService;
    
    /**
     * Process notification asynchronously (for real-time notifications)
     */
    @Async
    public CompletableFuture<Boolean> processAsync(Long notificationId) {
        try {
            log.debug("⚡ Processing notification {} asynchronously", notificationId);
            notificationService.sendNotification(notificationId);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("❌ Async processing failed for notification {}: {}", notificationId, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
    
    /**
     * Process queue asynchronously
     */
    @Async
    public CompletableFuture<Void> processQueueAsync() {
        log.debug("⚡ Starting async queue processing");
        queueService.processQueue();
        return CompletableFuture.completedFuture(null);
    }
}

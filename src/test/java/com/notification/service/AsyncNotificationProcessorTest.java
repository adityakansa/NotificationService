package com.notification.service;

import com.notification.dto.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AsyncNotificationProcessor
 */
@ExtendWith(MockitoExtension.class)
class AsyncNotificationProcessorTest {

    @Mock
    private NotificationQueueService queueService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AsyncNotificationProcessor asyncProcessor;

    @Test
    void testProcessAsync_Success() throws ExecutionException, InterruptedException {
        // Arrange
        Long notificationId = 1L;
        when(notificationService.sendNotification(notificationId)).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        CompletableFuture<Boolean> result = asyncProcessor.processAsync(notificationId);

        // Assert
        assertNotNull(result);
        assertTrue(result.get());
        verify(notificationService).sendNotification(notificationId);
    }

    @Test
    void testProcessAsync_Failure() throws ExecutionException, InterruptedException {
        // Arrange
        Long notificationId = 1L;
        doThrow(new RuntimeException("Send failed"))
            .when(notificationService).sendNotification(notificationId);

        // Act
        CompletableFuture<Boolean> result = asyncProcessor.processAsync(notificationId);

        // Assert
        assertNotNull(result);
        assertFalse(result.get());
        verify(notificationService).sendNotification(notificationId);
    }

    @Test
    void testProcessAsync_MultipleNotifications() throws ExecutionException, InterruptedException {
        // Arrange
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        CompletableFuture<Boolean> result1 = asyncProcessor.processAsync(1L);
        CompletableFuture<Boolean> result2 = asyncProcessor.processAsync(2L);
        CompletableFuture<Boolean> result3 = asyncProcessor.processAsync(3L);

        // Assert
        assertTrue(result1.get());
        assertTrue(result2.get());
        assertTrue(result3.get());
        verify(notificationService, times(3)).sendNotification(any());
    }

    @Test
    void testProcessQueueAsync_Success() throws ExecutionException, InterruptedException {
        // Arrange
        doNothing().when(queueService).processQueue();

        // Act
        CompletableFuture<Void> result = asyncProcessor.processQueueAsync();

        // Assert
        assertNotNull(result);
        result.get(); // Wait for completion
        verify(queueService).processQueue();
    }

    @Test
    void testProcessQueueAsync_WithException() {
        // Arrange
        doThrow(new RuntimeException("Queue processing failed"))
            .when(queueService).processQueue();

        // Act & Assert - Exception should propagate since method doesn't catch it
        assertThrows(RuntimeException.class, () -> {
            CompletableFuture<Void> result = asyncProcessor.processQueueAsync();
            result.get(); // This will throw if the async method throws
        });
    }

    @Test
    void testProcessAsync_NullNotificationId() throws ExecutionException, InterruptedException {
        // Arrange
        doThrow(new IllegalArgumentException("Notification ID cannot be null"))
            .when(notificationService).sendNotification(null);

        // Act
        CompletableFuture<Boolean> result = asyncProcessor.processAsync(null);

        // Assert
        assertNotNull(result);
        assertFalse(result.get());
        verify(notificationService).sendNotification(null);
    }

    @Test
    void testProcessAsync_VerifyAsyncExecution() throws ExecutionException, InterruptedException {
        // Arrange
        Long notificationId = 1L;
        when(notificationService.sendNotification(notificationId)).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        CompletableFuture<Boolean> future = asyncProcessor.processAsync(notificationId);

        // Assert - Future should be created
        assertNotNull(future);
        assertFalse(future.isCancelled());
        
        // Wait for completion
        Boolean result = future.get();
        assertTrue(result);
        assertTrue(future.isDone());
    }
}

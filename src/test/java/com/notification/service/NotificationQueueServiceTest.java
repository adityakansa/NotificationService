package com.notification.service;

import com.notification.domain.enums.NotificationPriority;
import com.notification.dto.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationQueueService
 */
@ExtendWith(MockitoExtension.class)
class NotificationQueueServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationQueueService queueService;

    @BeforeEach
    void setUp() {
        queueService.clearQueue();
    }

    @Test
    void testEnqueue_Success() {
        // Act
        queueService.enqueue(1L, NotificationPriority.HIGH);

        // Assert
        assertEquals(1, queueService.getQueueSize());
    }

    @Test
    void testEnqueue_MultipleNotifications() {
        // Act
        queueService.enqueue(1L, NotificationPriority.HIGH);
        queueService.enqueue(2L, NotificationPriority.MEDIUM);
        queueService.enqueue(3L, NotificationPriority.LOW);

        // Assert
        assertEquals(3, queueService.getQueueSize());
    }

    @Test
    void testProcessQueue_EmptyQueue() {
        // Act
        queueService.processQueue();

        // Assert
        verify(notificationService, never()).sendNotification(any());
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    void testProcessQueue_SingleNotification() {
        // Arrange
        queueService.enqueue(1L, NotificationPriority.HIGH);
        when(notificationService.sendNotification(1L)).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        queueService.processQueue();

        // Assert
        verify(notificationService).sendNotification(1L);
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    void testProcessQueue_HighPriorityFirst() {
        // Arrange
        queueService.enqueue(1L, NotificationPriority.LOW);
        queueService.enqueue(2L, NotificationPriority.HIGH);
        queueService.enqueue(3L, NotificationPriority.MEDIUM);
        
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        queueService.processQueue();

        // Assert
        verify(notificationService, times(3)).sendNotification(any());
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    void testProcessQueue_HandlesException() {
        // Arrange
        queueService.enqueue(1L, NotificationPriority.HIGH);
        queueService.enqueue(2L, NotificationPriority.HIGH);
        
        when(notificationService.sendNotification(1L)).thenThrow(new RuntimeException("Send failed"));
        when(notificationService.sendNotification(2L)).thenReturn(NotificationResponse.builder().id(2L).build());

        // Act
        queueService.processQueue();

        // Assert
        verify(notificationService).sendNotification(1L);
        verify(notificationService).sendNotification(2L);
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    void testGetQueueSize_Empty() {
        // Act
        int size = queueService.getQueueSize();

        // Assert
        assertEquals(0, size);
    }

    @Test
    void testGetQueueSize_AfterEnqueue() {
        // Arrange
        queueService.enqueue(1L, NotificationPriority.HIGH);
        queueService.enqueue(2L, NotificationPriority.MEDIUM);

        // Act
        int size = queueService.getQueueSize();

        // Assert
        assertEquals(2, size);
    }

    @Test
    void testClearQueue() {
        // Arrange
        queueService.enqueue(1L, NotificationPriority.HIGH);
        queueService.enqueue(2L, NotificationPriority.MEDIUM);
        assertEquals(2, queueService.getQueueSize());

        // Act
        queueService.clearQueue();

        // Assert
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    void testProcessQueue_AllPriorities() {
        // Arrange
        queueService.enqueue(1L, NotificationPriority.HIGH);
        queueService.enqueue(2L, NotificationPriority.HIGH);
        queueService.enqueue(3L, NotificationPriority.MEDIUM);
        queueService.enqueue(4L, NotificationPriority.MEDIUM);
        queueService.enqueue(5L, NotificationPriority.LOW);
        queueService.enqueue(6L, NotificationPriority.LOW);
        
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        queueService.processQueue();

        // Assert
        verify(notificationService, times(6)).sendNotification(any());
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    void testEnqueueAndProcess_Integration() {
        // Arrange
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        queueService.enqueue(1L, NotificationPriority.MEDIUM);
        assertEquals(1, queueService.getQueueSize());
        
        queueService.enqueue(2L, NotificationPriority.HIGH);
        assertEquals(2, queueService.getQueueSize());
        
        queueService.processQueue();

        // Assert
        assertEquals(0, queueService.getQueueSize());
        verify(notificationService, times(2)).sendNotification(any());
    }
}

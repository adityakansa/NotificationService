package com.notification.service;

import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import com.notification.dto.NotificationResponse;
import com.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationBatchService
 */
@ExtendWith(MockitoExtension.class)
class NotificationBatchServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationBatchService batchService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(batchService, "batchSize", 100);
        
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .active(true)
            .build();
    }

    @Test
    void testProcessBatchNotifications_NoPending_NoProcessing() {
        // Arrange
        when(notificationRepository.findByStatusInOrderByPriority(any()))
            .thenReturn(Collections.emptyList());

        // Act
        batchService.processBatchNotifications();

        // Assert
        verify(notificationRepository).findByStatusInOrderByPriority(any());
        verify(notificationService, never()).sendNotification(any());
    }

    @Test
    void testProcessBatchNotifications_HighPriorityFirst() {
        // Arrange
        Notification highPriority = createNotification(1L, NotificationPriority.HIGH);
        Notification mediumPriority = createNotification(2L, NotificationPriority.MEDIUM);
        Notification lowPriority = createNotification(3L, NotificationPriority.LOW);
        
        List<Notification> notifications = Arrays.asList(
            mediumPriority, highPriority, lowPriority
        );
        
        when(notificationRepository.findByStatusInOrderByPriority(any()))
            .thenReturn(notifications);
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        batchService.processBatchNotifications();

        // Assert
        verify(notificationService, times(3)).sendNotification(any());
    }

    @Test
    void testProcessBatchNotifications_HandlesExceptions() {
        // Arrange
        Notification notification1 = createNotification(1L, NotificationPriority.HIGH);
        Notification notification2 = createNotification(2L, NotificationPriority.HIGH);
        
        when(notificationRepository.findByStatusInOrderByPriority(any()))
            .thenReturn(Arrays.asList(notification1, notification2));
        
        // First notification fails, second should still be processed
        doThrow(new RuntimeException("Sending failed"))
            .when(notificationService).sendNotification(1L);
        when(notificationService.sendNotification(2L)).thenReturn(NotificationResponse.builder().id(2L).build());

        // Act
        batchService.processBatchNotifications();

        // Assert
        verify(notificationService).sendNotification(1L);
        verify(notificationService).sendNotification(2L);
    }

    @Test
    void testProcessBatchNotifications_MultipleBatches() {
        // Arrange
        ReflectionTestUtils.setField(batchService, "batchSize", 2);
        
        List<Notification> notifications = Arrays.asList(
            createNotification(1L, NotificationPriority.HIGH),
            createNotification(2L, NotificationPriority.HIGH),
            createNotification(3L, NotificationPriority.HIGH)
        );
        
        when(notificationRepository.findByStatusInOrderByPriority(any()))
            .thenReturn(notifications);
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        batchService.processBatchNotifications();

        // Assert
        verify(notificationService, times(3)).sendNotification(any());
    }

    @Test
    void testGetBatchStatistics_ReturnsCorrectData() {
        // Arrange
        when(notificationRepository.countByStatus(NotificationStatus.PENDING)).thenReturn(5L);
        when(notificationRepository.countByStatus(NotificationStatus.PROCESSING)).thenReturn(2L);
        when(notificationRepository.countByStatus(NotificationStatus.SENT)).thenReturn(10L);
        when(notificationRepository.countByStatus(NotificationStatus.FAILED)).thenReturn(1L);
        when(notificationRepository.countByStatus(NotificationStatus.RETRY)).thenReturn(3L);
        
        when(notificationRepository.findByStatusAndPriority(NotificationStatus.PENDING, NotificationPriority.HIGH))
            .thenReturn(Collections.emptyList());
        when(notificationRepository.findByStatusAndPriority(NotificationStatus.PENDING, NotificationPriority.MEDIUM))
            .thenReturn(Collections.emptyList());
        when(notificationRepository.findByStatusAndPriority(NotificationStatus.PENDING, NotificationPriority.LOW))
            .thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> stats = batchService.getBatchStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(100, stats.get("batchSize"));
        assertEquals(5L, stats.get("pendingCount"));
        assertEquals(2L, stats.get("processingCount"));
        assertEquals(10L, stats.get("sentCount"));
        assertEquals(1L, stats.get("failedCount"));
        assertEquals(3L, stats.get("retryCount"));
        assertNotNull(stats.get("priorityBreakdown"));
    }

    @Test
    void testProcessBatchNotifications_MixedPriorities() {
        // Arrange
        List<Notification> notifications = Arrays.asList(
            createNotification(1L, NotificationPriority.LOW),
            createNotification(2L, NotificationPriority.HIGH),
            createNotification(3L, NotificationPriority.MEDIUM),
            createNotification(4L, NotificationPriority.HIGH),
            createNotification(5L, NotificationPriority.MEDIUM)
        );
        
        when(notificationRepository.findByStatusInOrderByPriority(any()))
            .thenReturn(notifications);
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        batchService.processBatchNotifications();

        // Assert
        verify(notificationService, times(5)).sendNotification(any());
    }

    @Test
    void testProcessBatchNotifications_OnlyHighPriority() {
        // Arrange
        List<Notification> notifications = Arrays.asList(
            createNotification(1L, NotificationPriority.HIGH),
            createNotification(2L, NotificationPriority.HIGH)
        );
        
        when(notificationRepository.findByStatusInOrderByPriority(any()))
            .thenReturn(notifications);
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        batchService.processBatchNotifications();

        // Assert
        verify(notificationService, times(2)).sendNotification(any());
    }

    @Test
    void testProcessBatchNotifications_OnlyLowPriority() {
        // Arrange
        List<Notification> notifications = Arrays.asList(
            createNotification(1L, NotificationPriority.LOW),
            createNotification(2L, NotificationPriority.LOW)
        );
        
        when(notificationRepository.findByStatusInOrderByPriority(any()))
            .thenReturn(notifications);
        when(notificationService.sendNotification(any())).thenReturn(NotificationResponse.builder().id(1L).build());

        // Act
        batchService.processBatchNotifications();

        // Assert
        verify(notificationService, times(2)).sendNotification(any());
    }

    // Helper method
    private Notification createNotification(Long id, NotificationPriority priority) {
        return Notification.builder()
            .id(id)
            .user(testUser)
            .subject("Test")
            .body("Test body")
            .channel(NotificationChannel.EMAIL)
            .priority(priority)
            .status(NotificationStatus.PENDING)
            .build();
    }
}

package com.notification.service;

import com.notification.domain.entity.Notification;
import com.notification.domain.enums.NotificationStatus;
import com.notification.repository.NotificationHistoryRepository;
import com.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for NotificationRetryService
 * Tests exponential backoff and retry logic
 */
@ExtendWith(MockitoExtension.class)
class NotificationRetryServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private NotificationHistoryRepository historyRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private NotificationRetryService retryService;
    
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
            .id(1L)
            .status(NotificationStatus.RETRY)
            .currentRetryAttempt(0)
            .maxRetryAttempts(3)
            .nextRetryTime(LocalDateTime.now())
            .build();
        
        // Initialize @Value fields
        ReflectionTestUtils.setField(retryService, "maxRetryAttempts", 3);
        ReflectionTestUtils.setField(retryService, "initialRetryInterval", 1000L);
        ReflectionTestUtils.setField(retryService, "retryMultiplier", 2.0);
        ReflectionTestUtils.setField(retryService, "maxRetryInterval", 10000L);
    }
    
    @Test
    void testCalculateBackoffDelay_FirstAttempt() {
        // Act
        long delay = retryService.calculateBackoffDelay(0);
        
        // Assert
        assertEquals(1000L, delay); // Initial delay
    }
    
    @Test
    void testCalculateBackoffDelay_SecondAttempt() {
        // Act
        long delay = retryService.calculateBackoffDelay(1);
        
        // Assert
        assertEquals(2000L, delay); // 1000 * 2^1
    }
    
    @Test
    void testCalculateBackoffDelay_ThirdAttempt() {
        // Act
        long delay = retryService.calculateBackoffDelay(2);
        
        // Assert
        assertEquals(4000L, delay); // 1000 * 2^2
    }
    
    @Test
    void testCalculateBackoffDelay_MaxInterval() {
        // Act
        long delay = retryService.calculateBackoffDelay(10);
        
        // Assert
        assertEquals(10000L, delay); // Capped at max interval
    }
    
    @Test
    void testResetForRetry() {
        // Arrange
        testNotification.setCurrentRetryAttempt(2);
        testNotification.setStatus(NotificationStatus.FAILED);
        testNotification.setLastFailureReason("Test failure");
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        retryService.resetForRetry(1L);
        
        // Assert
        assertEquals(0, testNotification.getCurrentRetryAttempt());
        assertEquals(NotificationStatus.PENDING, testNotification.getStatus());
        assertNull(testNotification.getNextRetryTime());
        assertNull(testNotification.getLastFailureReason());
        verify(notificationRepository, times(1)).save(testNotification);
    }
    
    @Test
    void testManualRetry_Success() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // sendNotification returns NotificationResponse, not void
        when(notificationService.sendNotification(1L)).thenReturn(null);
        
        // Act
        retryService.manualRetry(1L);
        
        // Assert
        verify(notificationRepository, times(1)).save(any());
        verify(notificationService, times(1)).sendNotification(1L);
    }
    
    @Test
    void testManualRetry_AlreadySent() {
        // Arrange
        testNotification.setStatus(NotificationStatus.SENT);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            retryService.manualRetry(1L);
        });
    }
    
    @Test
    void testRetryNotification_MaxAttemptsExceeded() {
        // Arrange
        testNotification.setCurrentRetryAttempt(3);
        testNotification.setMaxRetryAttempts(3);
        
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Simulated failure"))
            .when(notificationService).sendNotification(1L);
        
        // Act
        retryService.retryNotification(testNotification);
        
        // Assert
        assertFalse(testNotification.canRetry());
        assertEquals(NotificationStatus.FAILED, testNotification.getStatus());
    }
}

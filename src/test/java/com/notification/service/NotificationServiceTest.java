package com.notification.service;

import com.notification.channel.DeliveryResult;
import com.notification.channel.NotificationChannelFactory;
import com.notification.channel.NotificationChannelStrategy;
import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.repository.NotificationHistoryRepository;
import com.notification.repository.NotificationRepository;
import com.notification.repository.UserRepository;
import com.notification.validation.ScheduledTimeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 * Tests core notification creation and sending logic
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private NotificationHistoryRepository historyRepository;
    
    @Mock
    private NotificationChannelFactory channelFactory;
    
    @Mock
    private NotificationChannelStrategy channelStrategy;
    
    @Mock
    private ScheduledTimeValidator scheduledTimeValidator;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private User testUser;
    private NotificationRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("+1234567890")
            .active(true)
            .preferredChannels(Set.of(NotificationChannel.EMAIL))
            .build();
        
        testRequest = NotificationRequest.builder()
            .userId(1L)
            .subject("Test Subject")
            .body("Test Body")
            .channel(NotificationChannel.EMAIL)
            .priority(NotificationPriority.MEDIUM)
            .build();
    }
    
    @Test
    void testCreateNotification_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return n;
            });
        
        // Act
        NotificationResponse response = notificationService.createNotification(testRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("Test Subject", response.getSubject());
        assertEquals(NotificationChannel.EMAIL, response.getChannel());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(historyRepository, times(1)).save(any());
    }
    
    @Test
    void testCreateNotification_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotification(testRequest);
        });
        
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testCreateNotification_InvalidChannel() {
        // Arrange
        testUser.setPreferredChannels(Set.of(NotificationChannel.SMS)); // Only SMS allowed
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotification(testRequest);
        });
    }
    
    @Test
    void testSendNotification_Success() {
        // Arrange
        Notification notification = Notification.builder()
            .id(1L)
            .user(testUser)
            .subject("Test Subject")
            .body("Test Body")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PENDING)
            .build();
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(channelFactory.getStrategy(NotificationChannel.EMAIL)).thenReturn(channelStrategy);
        when(channelStrategy.send(any(), any())).thenReturn(DeliveryResult.success("Sent successfully"));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        NotificationResponse response = notificationService.sendNotification(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals(NotificationStatus.SENT, response.getStatus());
        verify(channelStrategy, times(1)).send(any(), any());
        verify(notificationRepository, times(2)).save(any()); // Once for processing, once for sent
    }
    
    @Test
    void testSendNotification_Failure() {
        // Arrange
        Notification notification = Notification.builder()
            .id(1L)
            .user(testUser)
            .subject("Test Subject")
            .body("Test Body")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PENDING)
            .maxRetryAttempts(3)
            .currentRetryAttempt(0)
            .build();
        
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(channelFactory.getStrategy(NotificationChannel.EMAIL)).thenReturn(channelStrategy);
        when(channelStrategy.send(any(), any())).thenReturn(
            DeliveryResult.failure("Failed to send", "Network error")
        );
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        NotificationResponse response = notificationService.sendNotification(1L);
        
        // Assert
        assertNotNull(response);
        assertEquals(NotificationStatus.RETRY, response.getStatus());
        assertEquals(1, response.getCurrentRetryAttempt());
        assertNotNull(response.getLastFailureReason());
    }
    
    @Test
    void testPriorityHandling() {
        // Test that high priority notifications are handled correctly
        NotificationRequest highPriorityRequest = NotificationRequest.builder()
            .userId(1L)
            .subject("Urgent")
            .body("High priority message")
            .channel(NotificationChannel.EMAIL)
            .priority(NotificationPriority.HIGH)
            .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(1L);
                return n;
            });
        
        NotificationResponse response = notificationService.createNotification(highPriorityRequest);
        
        assertEquals(NotificationPriority.HIGH, response.getPriority());
    }
}

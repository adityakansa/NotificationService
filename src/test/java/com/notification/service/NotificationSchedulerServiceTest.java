package com.notification.service;

import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationStatus;
import com.notification.repository.NotificationRepository;
import com.notification.validation.ScheduledTimeValidator;
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

/**
 * Unit tests for NotificationSchedulerService
 * Tests scheduling, rescheduling, and cancellation logic with validation
 */
@ExtendWith(MockitoExtension.class)
class NotificationSchedulerServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private ScheduledTimeValidator scheduledTimeValidator;
    
    @InjectMocks
    private NotificationSchedulerService schedulerService;
    
    private User testUser;
    private Notification scheduledNotification;
    private Notification sentNotification;
    private Notification failedNotification;
    private Notification cancelledNotification;
    private Notification processingNotification;
    private Notification pendingNotification;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        
        scheduledNotification = Notification.builder()
            .id(1L)
            .user(testUser)
            .subject("Test")
            .body("Test body")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SCHEDULED)
            .scheduledTime(LocalDateTime.now().plusHours(1))
            .build();
        
        sentNotification = Notification.builder()
            .id(2L)
            .user(testUser)
            .subject("Sent")
            .body("Already sent")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduledTime(LocalDateTime.now().minusHours(1))
            .sentAt(LocalDateTime.now().minusMinutes(30))
            .build();
        
        failedNotification = Notification.builder()
            .id(3L)
            .user(testUser)
            .subject("Failed")
            .body("Failed notification")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.FAILED)
            .scheduledTime(LocalDateTime.now().minusHours(1))
            .build();
        
        cancelledNotification = Notification.builder()
            .id(4L)
            .user(testUser)
            .subject("Cancelled")
            .body("Cancelled notification")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.CANCELLED)
            .scheduledTime(LocalDateTime.now().plusHours(1))
            .build();
        
        processingNotification = Notification.builder()
            .id(5L)
            .user(testUser)
            .subject("Processing")
            .body("Currently processing")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PROCESSING)
            .scheduledTime(LocalDateTime.now())
            .build();
        
        pendingNotification = Notification.builder()
            .id(6L)
            .user(testUser)
            .subject("Pending")
            .body("Pending notification")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PENDING)
            .build();
    }
    
    // ==================== Reschedule Tests ====================
    
    @Test
    void testRescheduleNotification_Success_ScheduledStatus() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(scheduledNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(scheduledNotification);
        
        // Act
        Notification result = schedulerService.rescheduleNotification(1L, newTime);
        
        // Assert
        assertNotNull(result);
        assertEquals(NotificationStatus.SCHEDULED, result.getStatus());
        verify(notificationRepository).save(scheduledNotification);
    }
    
    @Test
    void testRescheduleNotification_Success_PendingStatus() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);
        when(notificationRepository.findById(6L)).thenReturn(Optional.of(pendingNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(pendingNotification);
        
        // Act
        Notification result = schedulerService.rescheduleNotification(6L, newTime);
        
        // Assert
        assertNotNull(result);
        assertEquals(NotificationStatus.SCHEDULED, result.getStatus());
        verify(notificationRepository).save(pendingNotification);
    }
    
    @Test
    void testRescheduleNotification_NullTime_ThrowsException() {
        // Arrange - configure validator to throw exception
        doThrow(new IllegalArgumentException("New scheduled time is required"))
            .when(scheduledTimeValidator).validateRescheduleTime(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> schedulerService.rescheduleNotification(1L, null)
        );
        
        assertEquals("New scheduled time is required", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testRescheduleNotification_PastTime_ThrowsException() {
        // Arrange - configure validator to throw exception
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        doThrow(new IllegalArgumentException("New scheduled time must be in the future"))
            .when(scheduledTimeValidator).validateRescheduleTime(pastTime);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> schedulerService.rescheduleNotification(1L, pastTime)
        );
        
        assertEquals("New scheduled time must be in the future", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testRescheduleNotification_NotificationNotFound_ThrowsException() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> schedulerService.rescheduleNotification(999L, newTime)
        );
        
        assertEquals("Notification not found: 999", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testRescheduleNotification_SentStatus_ThrowsException() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(sentNotification));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> schedulerService.rescheduleNotification(2L, newTime)
        );
        
        assertEquals("Cannot reschedule notification that has already been sent", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testRescheduleNotification_FailedStatus_ThrowsException() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);
        when(notificationRepository.findById(3L)).thenReturn(Optional.of(failedNotification));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> schedulerService.rescheduleNotification(3L, newTime)
        );
        
        assertEquals("Cannot reschedule failed notification. Please reset it first", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testRescheduleNotification_CancelledStatus_ThrowsException() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);
        when(notificationRepository.findById(4L)).thenReturn(Optional.of(cancelledNotification));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> schedulerService.rescheduleNotification(4L, newTime)
        );
        
        assertEquals("Cannot reschedule cancelled notification", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testRescheduleNotification_ProcessingStatus_ThrowsException() {
        // Arrange
        LocalDateTime newTime = LocalDateTime.now().plusHours(2);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(processingNotification));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> schedulerService.rescheduleNotification(5L, newTime)
        );
        
        assertEquals("Cannot reschedule notification that is currently being processed", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    // ==================== Cancel Tests ====================
    
    @Test
    void testCancelScheduledNotification_Success() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(scheduledNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(scheduledNotification);
        
        // Act
        schedulerService.cancelScheduledNotification(1L);
        
        // Assert
        verify(notificationRepository).save(scheduledNotification);
        assertEquals(NotificationStatus.CANCELLED, scheduledNotification.getStatus());
    }
    
    @Test
    void testCancelScheduledNotification_NotificationNotFound_ThrowsException() {
        // Arrange
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> schedulerService.cancelScheduledNotification(999L)
        );
        
        assertEquals("Notification not found: 999", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testCancelScheduledNotification_NotScheduledStatus_ThrowsException() {
        // Arrange
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(sentNotification));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> schedulerService.cancelScheduledNotification(2L)
        );
        
        assertEquals("Notification is not in scheduled state", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    @Test
    void testCancelScheduledNotification_AlreadyCancelled_ThrowsException() {
        // Arrange
        when(notificationRepository.findById(4L)).thenReturn(Optional.of(cancelledNotification));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> schedulerService.cancelScheduledNotification(4L)
        );
        
        assertEquals("Notification is not in scheduled state", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }
    
    // ==================== Schedule Tests ====================
    
    @Test
    void testScheduleNotification_Success() {
        // Arrange
        Notification newNotification = Notification.builder()
            .id(7L)
            .user(testUser)
            .subject("New scheduled")
            .body("New body")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PENDING)
            .scheduledTime(LocalDateTime.now().plusHours(1))
            .build();
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(newNotification);
        
        // Act
        Notification result = schedulerService.scheduleNotification(newNotification);
        
        // Assert
        assertNotNull(result);
        assertEquals(NotificationStatus.SCHEDULED, newNotification.getStatus());
        verify(notificationRepository).save(newNotification);
    }
    
    // ==================== Recurring Notification Tests ====================
    
    @Test
    void testProcessRecurringNotifications_HourlyFrequency_Success() {
        // Arrange
        Notification recurringNotification = Notification.builder()
            .id(10L)
            .user(testUser)
            .subject("Hourly Report")
            .body("Hourly notification")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(LocalDateTime.now().minusHours(1))
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.HOURLY)
            .maxOccurrences(10)
            .occurrenceCount(1)
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class)); // Original + new occurrence
        assertEquals(2, recurringNotification.getOccurrenceCount());
    }
    
    @Test
    void testProcessRecurringNotifications_DailyFrequency_Success() {
        // Arrange
        Notification recurringNotification = Notification.builder()
            .id(11L)
            .user(testUser)
            .subject("Daily Reminder")
            .body("Daily notification")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(LocalDateTime.now().minusDays(1))
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.DAILY)
            .maxOccurrences(30)
            .occurrenceCount(5)
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
        assertEquals(6, recurringNotification.getOccurrenceCount());
    }
    
    @Test
    void testProcessRecurringNotifications_WeeklyFrequency_Success() {
        // Arrange
        Notification recurringNotification = Notification.builder()
            .id(12L)
            .user(testUser)
            .subject("Weekly Summary")
            .body("Weekly notification")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(LocalDateTime.now().minusWeeks(1))
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.WEEKLY)
            .maxOccurrences(52)
            .occurrenceCount(10)
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
        assertEquals(11, recurringNotification.getOccurrenceCount());
    }
    
    @Test
    void testProcessRecurringNotifications_MonthlyFrequency_Success() {
        // Arrange
        Notification recurringNotification = Notification.builder()
            .id(13L)
            .user(testUser)
            .subject("Monthly Invoice")
            .body("Monthly notification")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(LocalDateTime.now().minusMonths(1))
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.MONTHLY)
            .maxOccurrences(12)
            .occurrenceCount(3)
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
        assertEquals(4, recurringNotification.getOccurrenceCount());
    }
    
    @Test
    void testProcessRecurringNotifications_MaxOccurrencesReached_StopsRecurrence() {
        // Arrange
        Notification recurringNotification = Notification.builder()
            .id(14L)
            .user(testUser)
            .subject("Limited Recurring")
            .body("Should stop")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(LocalDateTime.now().minusHours(1))
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.HOURLY)
            .maxOccurrences(5)
            .occurrenceCount(5) // Already at max
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class)); // Should not create new occurrence
        assertEquals(5, recurringNotification.getOccurrenceCount()); // Count unchanged
    }
    
    @Test
    void testProcessRecurringNotifications_RecurrenceEndTimeReached_StopsRecurrence() {
        // Arrange
        Notification recurringNotification = Notification.builder()
            .id(15L)
            .user(testUser)
            .subject("Time Limited Recurring")
            .body("Should stop")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(LocalDateTime.now().minusDays(2))
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.DAILY)
            .recurrenceEndTime(LocalDateTime.now().minusDays(1)) // End time in past
            .occurrenceCount(1)
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class)); // Should not create new occurrence
    }
    
    @Test
    void testProcessRecurringNotifications_NextOccurrenceAfterEndTime_SkipsCreation() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Notification recurringNotification = Notification.builder()
            .id(16L)
            .user(testUser)
            .subject("Ending Soon")
            .body("Next would be past end time")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(now.minusDays(10)) // 10 days ago
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.WEEKLY)
            .recurrenceEndTime(now.minusDays(2)) // End time 2 days ago
            .maxOccurrences(10)
            .occurrenceCount(1)
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert - Next occurrence would be 3 days ago (after end time), so should skip
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void testProcessRecurringNotifications_EmptyList_NoProcessing() {
        // Arrange
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of());
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void testProcessRecurringNotifications_CreatesScheduledNotification() {
        // Arrange
        LocalDateTime originalTime = LocalDateTime.now().minusHours(1);
        Notification recurringNotification = Notification.builder()
            .id(17L)
            .user(testUser)
            .subject("Test Recurring")
            .body("Test body")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .scheduleType(com.notification.domain.enums.ScheduleType.RECURRING)
            .scheduledTime(originalTime)
            .recurrenceFrequency(com.notification.domain.enums.RecurrenceFrequency.HOURLY)
            .maxOccurrences(5)
            .occurrenceCount(1)
            .build();
        
        when(notificationRepository.findRecurringNotificationsForNextExecution())
            .thenReturn(java.util.List.of(recurringNotification));
        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification saved = invocation.getArgument(0);
                // Verify new occurrence has SCHEDULED status and correct scheduled time
                if (saved.getId() == null) { // New notification
                    assertEquals(NotificationStatus.SCHEDULED, saved.getStatus());
                    assertEquals(com.notification.domain.enums.ScheduleType.RECURRING, saved.getScheduleType());
                    assertNotNull(saved.getScheduledTime());
                    // Should be approximately 1 hour after original time
                    assertTrue(saved.getScheduledTime().isAfter(originalTime));
                }
                return saved;
            });
        
        // Act
        schedulerService.processRecurringNotifications();
        
        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}


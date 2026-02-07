package com.notification.domain.entity;

import com.notification.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Notification entity
 */
class NotificationTest {

    private Notification notification;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .active(true)
            .build();

        notification = Notification.builder()
            .id(1L)
            .user(testUser)
            .subject("Test Subject")
            .body("Test Body")
            .channel(NotificationChannel.EMAIL)
            .priority(NotificationPriority.MEDIUM)
            .status(NotificationStatus.PENDING)
            .scheduleType(ScheduleType.IMMEDIATE)
            .maxRetryAttempts(3)
            .currentRetryAttempt(0)
            .metadata(new HashMap<>())
            .build();
    }

    @Test
    void testNotificationBuilder() {
        assertNotNull(notification);
        assertEquals(1L, notification.getId());
        assertEquals("Test Subject", notification.getSubject());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
    }

    @Test
    void testMarkAsSent() {
        notification.markAsSent();
        
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertNotNull(notification.getSentAt());
        assertEquals(0, notification.getCurrentRetryAttempt());
    }

    @Test
    void testMarkAsFailed() {
        String reason = "Network error";
        notification.markAsFailed(reason);
        
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertEquals(reason, notification.getLastFailureReason());
        assertNotNull(notification.getFailedAt());
    }

    @Test
    void testRecordRetryAttempt_CanRetry() {
        String reason = "Temporary failure";
        notification.recordRetryAttempt(reason);
        
        assertEquals(1, notification.getCurrentRetryAttempt());
        assertEquals(NotificationStatus.RETRY, notification.getStatus());
        assertEquals(reason, notification.getLastFailureReason());
        assertNotNull(notification.getNextRetryTime());
    }

    @Test
    void testRecordRetryAttempt_MaxRetriesReached() {
        notification.setCurrentRetryAttempt(2); // Already at 2, max is 3
        
        notification.recordRetryAttempt("Final failure");
        
        assertEquals(3, notification.getCurrentRetryAttempt());
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertTrue(notification.getLastFailureReason().contains("Max retry attempts exceeded"));
    }

    @Test
    void testCanRetry_True() {
        notification.setCurrentRetryAttempt(1);
        notification.setMaxRetryAttempts(3);
        
        assertTrue(notification.canRetry());
    }

    @Test
    void testCanRetry_False() {
        notification.setCurrentRetryAttempt(3);
        notification.setMaxRetryAttempts(3);
        
        assertFalse(notification.canRetry());
    }

    @Test
    void testIncrementOccurrence() {
        notification.setOccurrenceCount(5);
        notification.incrementOccurrence();
        
        assertEquals(6, notification.getOccurrenceCount());
    }

    @Test
    void testShouldContinueRecurrence_NotRecurring() {
        notification.setScheduleType(ScheduleType.IMMEDIATE);
        
        assertFalse(notification.shouldContinueRecurrence());
    }

    @Test
    void testShouldContinueRecurrence_MaxOccurrencesReached() {
        notification.setScheduleType(ScheduleType.RECURRING);
        notification.setMaxOccurrences(5);
        notification.setOccurrenceCount(5);
        
        assertFalse(notification.shouldContinueRecurrence());
    }

    @Test
    void testShouldContinueRecurrence_EndTimeReached() {
        notification.setScheduleType(ScheduleType.RECURRING);
        notification.setRecurrenceEndTime(LocalDateTime.now().minusDays(1));
        
        assertFalse(notification.shouldContinueRecurrence());
    }

    @Test
    void testShouldContinueRecurrence_Success() {
        notification.setScheduleType(ScheduleType.RECURRING);
        notification.setMaxOccurrences(10);
        notification.setOccurrenceCount(3);
        notification.setRecurrenceEndTime(LocalDateTime.now().plusDays(7));
        
        assertTrue(notification.shouldContinueRecurrence());
    }

    @Test
    void testPrePersist() {
        Notification newNotification = new Notification();
        newNotification.onCreate();
        
        assertNotNull(newNotification.getCreatedAt());
        assertNotNull(newNotification.getUpdatedAt());
    }

    @Test
    void testPreUpdate() {
        LocalDateTime originalTime = LocalDateTime.now().minusMinutes(5);
        notification.setUpdatedAt(originalTime);
        
        notification.onUpdate();
        
        assertTrue(notification.getUpdatedAt().isAfter(originalTime));
    }

    @Test
    void testMetadataManagement() {
        notification.getMetadata().put("key1", "value1");
        notification.getMetadata().put("key2", "value2");
        
        assertEquals("value1", notification.getMetadata().get("key1"));
        assertEquals(2, notification.getMetadata().size());
    }

    @Test
    void testRecurringNotificationFields() {
        notification.setScheduleType(ScheduleType.RECURRING);
        notification.setRecurrenceFrequency(RecurrenceFrequency.DAILY);
        notification.setMaxOccurrences(30);
        notification.setRecurrenceEndTime(LocalDateTime.now().plusMonths(1));
        
        assertEquals(ScheduleType.RECURRING, notification.getScheduleType());
        assertEquals(RecurrenceFrequency.DAILY, notification.getRecurrenceFrequency());
        assertEquals(30, notification.getMaxOccurrences());
        assertNotNull(notification.getRecurrenceEndTime());
    }

    @Test
    void testScheduledNotificationFields() {
        LocalDateTime scheduleTime = LocalDateTime.now().plusHours(2);
        notification.setScheduleType(ScheduleType.SCHEDULED);
        notification.setScheduledTime(scheduleTime);
        
        assertEquals(ScheduleType.SCHEDULED, notification.getScheduleType());
        assertEquals(scheduleTime, notification.getScheduledTime());
    }

    @Test
    void testAllNotificationStatuses() {
        notification.setStatus(NotificationStatus.PENDING);
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        
        notification.setStatus(NotificationStatus.PROCESSING);
        assertEquals(NotificationStatus.PROCESSING, notification.getStatus());
        
        notification.setStatus(NotificationStatus.SENT);
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        
        notification.setStatus(NotificationStatus.FAILED);
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        
        notification.setStatus(NotificationStatus.RETRY);
        assertEquals(NotificationStatus.RETRY, notification.getStatus());
        
        notification.setStatus(NotificationStatus.SCHEDULED);
        assertEquals(NotificationStatus.SCHEDULED, notification.getStatus());
        
        notification.setStatus(NotificationStatus.CANCELLED);
        assertEquals(NotificationStatus.CANCELLED, notification.getStatus());
    }

    @Test
    void testAllNotificationPriorities() {
        notification.setPriority(NotificationPriority.LOW);
        assertEquals(NotificationPriority.LOW, notification.getPriority());
        
        notification.setPriority(NotificationPriority.MEDIUM);
        assertEquals(NotificationPriority.MEDIUM, notification.getPriority());
        
        notification.setPriority(NotificationPriority.HIGH);
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
    }

    @Test
    void testAllNotificationChannels() {
        notification.setChannel(NotificationChannel.EMAIL);
        assertEquals(NotificationChannel.EMAIL, notification.getChannel());
        
        notification.setChannel(NotificationChannel.SMS);
        assertEquals(NotificationChannel.SMS, notification.getChannel());
        
        notification.setChannel(NotificationChannel.PUSH);
        assertEquals(NotificationChannel.PUSH, notification.getChannel());
        
        notification.setChannel(NotificationChannel.WHATSAPP);
        assertEquals(NotificationChannel.WHATSAPP, notification.getChannel());
        
        notification.setChannel(NotificationChannel.SLACK);
        assertEquals(NotificationChannel.SLACK, notification.getChannel());
    }
}

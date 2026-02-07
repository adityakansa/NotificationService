package com.notification.domain.entity;

import com.notification.domain.enums.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationHistory entity
 */
class NotificationHistoryTest {

    private NotificationHistory history;

    @BeforeEach
    void setUp() {
        history = NotificationHistory.builder()
            .id(1L)
            .notificationId(100L)
            .status(NotificationStatus.SENT)
            .attemptNumber(1)
            .message("Notification sent successfully")
            .build();
    }

    @Test
    void testNotificationHistoryBuilder() {
        assertNotNull(history);
        assertEquals(1L, history.getId());
        assertEquals(100L, history.getNotificationId());
        assertEquals(NotificationStatus.SENT, history.getStatus());
        assertEquals(1, history.getAttemptNumber());
        assertEquals("Notification sent successfully", history.getMessage());
    }

    @Test
    void testPrePersist() {
        NotificationHistory newHistory = new NotificationHistory();
        newHistory.onCreate();
        
        assertNotNull(newHistory.getCreatedAt());
        assertTrue(newHistory.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        NotificationHistory fullHistory = new NotificationHistory(
            1L,
            100L,
            NotificationStatus.FAILED,
            2,
            "Delivery failed",
            "Network timeout",
            now
        );
        
        assertNotNull(fullHistory);
        assertEquals(1L, fullHistory.getId());
        assertEquals(100L, fullHistory.getNotificationId());
        assertEquals(NotificationStatus.FAILED, fullHistory.getStatus());
        assertEquals(2, fullHistory.getAttemptNumber());
        assertEquals("Delivery failed", fullHistory.getMessage());
        assertEquals("Network timeout", fullHistory.getErrorDetails());
        assertEquals(now, fullHistory.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        NotificationHistory emptyHistory = new NotificationHistory();
        assertNotNull(emptyHistory);
        assertNull(emptyHistory.getId());
        assertNull(emptyHistory.getNotificationId());
    }

    @Test
    void testSettersAndGetters() {
        history.setId(2L);
        assertEquals(2L, history.getId());
        
        history.setNotificationId(200L);
        assertEquals(200L, history.getNotificationId());
        
        history.setStatus(NotificationStatus.RETRY);
        assertEquals(NotificationStatus.RETRY, history.getStatus());
        
        history.setAttemptNumber(3);
        assertEquals(3, history.getAttemptNumber());
        
        history.setMessage("New message");
        assertEquals("New message", history.getMessage());
        
        history.setErrorDetails("New error");
        assertEquals("New error", history.getErrorDetails());
    }

    @Test
    void testErrorDetails() {
        history.setErrorDetails("Connection refused: connect");
        assertEquals("Connection refused: connect", history.getErrorDetails());
    }

    @Test
    void testMultipleAttempts() {
        NotificationHistory attempt1 = NotificationHistory.builder()
            .notificationId(1L)
            .status(NotificationStatus.FAILED)
            .attemptNumber(1)
            .errorDetails("Temporary failure")
            .build();
        
        NotificationHistory attempt2 = NotificationHistory.builder()
            .notificationId(1L)
            .status(NotificationStatus.RETRY)
            .attemptNumber(2)
            .message("Retrying")
            .build();
        
        NotificationHistory attempt3 = NotificationHistory.builder()
            .notificationId(1L)
            .status(NotificationStatus.SENT)
            .attemptNumber(3)
            .message("Sent successfully")
            .build();
        
        assertEquals(1, attempt1.getAttemptNumber());
        assertEquals(2, attempt2.getAttemptNumber());
        assertEquals(3, attempt3.getAttemptNumber());
        
        assertEquals(NotificationStatus.FAILED, attempt1.getStatus());
        assertEquals(NotificationStatus.RETRY, attempt2.getStatus());
        assertEquals(NotificationStatus.SENT, attempt3.getStatus());
    }

    @Test
    void testAllStatuses() {
        history.setStatus(NotificationStatus.PENDING);
        assertEquals(NotificationStatus.PENDING, history.getStatus());
        
        history.setStatus(NotificationStatus.PROCESSING);
        assertEquals(NotificationStatus.PROCESSING, history.getStatus());
        
        history.setStatus(NotificationStatus.SENT);
        assertEquals(NotificationStatus.SENT, history.getStatus());
        
        history.setStatus(NotificationStatus.FAILED);
        assertEquals(NotificationStatus.FAILED, history.getStatus());
        
        history.setStatus(NotificationStatus.RETRY);
        assertEquals(NotificationStatus.RETRY, history.getStatus());
        
        history.setStatus(NotificationStatus.CANCELLED);
        assertEquals(NotificationStatus.CANCELLED, history.getStatus());
    }

    @Test
    void testLongMessage() {
        String longMessage = "A".repeat(1000);
        history.setMessage(longMessage);
        assertEquals(1000, history.getMessage().length());
    }

    @Test
    void testLongErrorDetails() {
        String longError = "E".repeat(2000);
        history.setErrorDetails(longError);
        assertEquals(2000, history.getErrorDetails().length());
    }
}

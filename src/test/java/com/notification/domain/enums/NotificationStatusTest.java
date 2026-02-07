package com.notification.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationStatus enum
 */
class NotificationStatusTest {

    @Test
    void testAllStatuses() {
        assertEquals(7, NotificationStatus.values().length);
    }

    @Test
    void testPendingStatus() {
        assertNotNull(NotificationStatus.PENDING);
        assertEquals("PENDING", NotificationStatus.PENDING.name());
    }

    @Test
    void testScheduledStatus() {
        assertNotNull(NotificationStatus.SCHEDULED);
        assertEquals("SCHEDULED", NotificationStatus.SCHEDULED.name());
    }

    @Test
    void testProcessingStatus() {
        assertNotNull(NotificationStatus.PROCESSING);
        assertEquals("PROCESSING", NotificationStatus.PROCESSING.name());
    }

    @Test
    void testSentStatus() {
        assertNotNull(NotificationStatus.SENT);
        assertEquals("SENT", NotificationStatus.SENT.name());
    }

    @Test
    void testFailedStatus() {
        assertNotNull(NotificationStatus.FAILED);
        assertEquals("FAILED", NotificationStatus.FAILED.name());
    }

    @Test
    void testRetryStatus() {
        assertNotNull(NotificationStatus.RETRY);
        assertEquals("RETRY", NotificationStatus.RETRY.name());
    }

    @Test
    void testCancelledStatus() {
        assertNotNull(NotificationStatus.CANCELLED);
        assertEquals("CANCELLED", NotificationStatus.CANCELLED.name());
    }

    @Test
    void testValueOf() {
        assertEquals(NotificationStatus.PENDING, NotificationStatus.valueOf("PENDING"));
        assertEquals(NotificationStatus.SENT, NotificationStatus.valueOf("SENT"));
        assertEquals(NotificationStatus.FAILED, NotificationStatus.valueOf("FAILED"));
    }
}

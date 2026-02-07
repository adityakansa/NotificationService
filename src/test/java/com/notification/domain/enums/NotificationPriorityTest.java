package com.notification.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationPriority enum
 */
class NotificationPriorityTest {

    @Test
    void testAllPriorities() {
        assertEquals(3, NotificationPriority.values().length);
    }

    @Test
    void testHighPriority() {
        assertNotNull(NotificationPriority.HIGH);
        assertEquals("HIGH", NotificationPriority.HIGH.name());
    }

    @Test
    void testMediumPriority() {
        assertNotNull(NotificationPriority.MEDIUM);
        assertEquals("MEDIUM", NotificationPriority.MEDIUM.name());
    }

    @Test
    void testLowPriority() {
        assertNotNull(NotificationPriority.LOW);
        assertEquals("LOW", NotificationPriority.LOW.name());
    }

    @Test
    void testValueOf() {
        assertEquals(NotificationPriority.HIGH, NotificationPriority.valueOf("HIGH"));
        assertEquals(NotificationPriority.MEDIUM, NotificationPriority.valueOf("MEDIUM"));
        assertEquals(NotificationPriority.LOW, NotificationPriority.valueOf("LOW"));
    }

    @Test
    void testPriorityOrdering() {
        NotificationPriority[] priorities = NotificationPriority.values();
        assertEquals(NotificationPriority.HIGH, priorities[0]);
        assertEquals(NotificationPriority.MEDIUM, priorities[1]);
        assertEquals(NotificationPriority.LOW, priorities[2]);
    }
}

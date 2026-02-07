package com.notification.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScheduleType enum
 */
class ScheduleTypeTest {

    @Test
    void testAllScheduleTypes() {
        assertEquals(3, ScheduleType.values().length);
    }

    @Test
    void testImmediateType() {
        assertNotNull(ScheduleType.IMMEDIATE);
        assertEquals("IMMEDIATE", ScheduleType.IMMEDIATE.name());
    }

    @Test
    void testScheduledType() {
        assertNotNull(ScheduleType.SCHEDULED);
        assertEquals("SCHEDULED", ScheduleType.SCHEDULED.name());
    }

    @Test
    void testRecurringType() {
        assertNotNull(ScheduleType.RECURRING);
        assertEquals("RECURRING", ScheduleType.RECURRING.name());
    }

    @Test
    void testValueOf() {
        assertEquals(ScheduleType.IMMEDIATE, ScheduleType.valueOf("IMMEDIATE"));
        assertEquals(ScheduleType.SCHEDULED, ScheduleType.valueOf("SCHEDULED"));
        assertEquals(ScheduleType.RECURRING, ScheduleType.valueOf("RECURRING"));
    }

    @Test
    void testTypeOrdering() {
        ScheduleType[] types = ScheduleType.values();
        assertEquals(ScheduleType.IMMEDIATE, types[0]);
        assertEquals(ScheduleType.SCHEDULED, types[1]);
        assertEquals(ScheduleType.RECURRING, types[2]);
    }
}

package com.notification.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RecurrenceFrequency enum
 */
class RecurrenceFrequencyTest {

    @Test
    void testAllFrequencies() {
        assertEquals(5, RecurrenceFrequency.values().length);
    }

    @Test
    void testMinutelyFrequency() {
        assertEquals(60000L, RecurrenceFrequency.MINUTELY.getIntervalMillis());
    }

    @Test
    void testHourlyFrequency() {
        assertEquals(3600000L, RecurrenceFrequency.HOURLY.getIntervalMillis());
    }

    @Test
    void testDailyFrequency() {
        assertEquals(86400000L, RecurrenceFrequency.DAILY.getIntervalMillis());
    }

    @Test
    void testWeeklyFrequency() {
        assertEquals(604800000L, RecurrenceFrequency.WEEKLY.getIntervalMillis());
    }

    @Test
    void testMonthlyFrequency() {
        assertEquals(2592000000L, RecurrenceFrequency.MONTHLY.getIntervalMillis());
    }

    @Test
    void testValueOf() {
        assertEquals(RecurrenceFrequency.MINUTELY, RecurrenceFrequency.valueOf("MINUTELY"));
        assertEquals(RecurrenceFrequency.HOURLY, RecurrenceFrequency.valueOf("HOURLY"));
        assertEquals(RecurrenceFrequency.DAILY, RecurrenceFrequency.valueOf("DAILY"));
        assertEquals(RecurrenceFrequency.WEEKLY, RecurrenceFrequency.valueOf("WEEKLY"));
        assertEquals(RecurrenceFrequency.MONTHLY, RecurrenceFrequency.valueOf("MONTHLY"));
    }

    @Test
    void testIntervalMillisValues() {
        // Verify calculations
        assertEquals(60 * 1000L, RecurrenceFrequency.MINUTELY.getIntervalMillis()); // 1 minute
        assertEquals(3600 * 1000L, RecurrenceFrequency.HOURLY.getIntervalMillis()); // 1 hour
        assertEquals(24 * 3600 * 1000L, RecurrenceFrequency.DAILY.getIntervalMillis()); // 24 hours
        assertEquals(7 * 24 * 3600 * 1000L, RecurrenceFrequency.WEEKLY.getIntervalMillis()); // 7 days
        assertEquals(30L * 24 * 3600 * 1000, RecurrenceFrequency.MONTHLY.getIntervalMillis()); // 30 days
    }
}

package com.notification.domain.enums;

/**
 * Frequency for recurring notifications
 */
public enum RecurrenceFrequency {
    MINUTELY("Minutely", 60 * 1000L),
    HOURLY("Hourly", 60 * 60 * 1000L),
    DAILY("Daily", 24 * 60 * 60 * 1000L),
    WEEKLY("Weekly", 7 * 24 * 60 * 60 * 1000L),
    MONTHLY("Monthly", 30L * 24 * 60 * 60 * 1000L);

    private final String displayName;
    private final long intervalMillis;

    RecurrenceFrequency(String displayName, long intervalMillis) {
        this.displayName = displayName;
        this.intervalMillis = intervalMillis;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }
}

package com.notification.domain.enums;

/**
 * Types of notification scheduling
 */
public enum ScheduleType {
    IMMEDIATE("Immediate", "Send immediately"),
    SCHEDULED("Scheduled", "Send at specific time"),
    RECURRING("Recurring", "Send periodically");

    private final String displayName;
    private final String description;

    ScheduleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

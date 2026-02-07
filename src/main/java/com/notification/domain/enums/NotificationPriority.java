package com.notification.domain.enums;

/**
 * Priority levels for notifications
 * HIGH - Critical notifications, sent immediately
 * MEDIUM - Important notifications, sent with normal priority
 * LOW - Non-urgent notifications, can be batched
 */
public enum NotificationPriority {
    HIGH(1, "High Priority"),
    MEDIUM(2, "Medium Priority"),
    LOW(3, "Low Priority");

    private final int level;
    private final String description;

    NotificationPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHigherThan(NotificationPriority other) {
        return this.level < other.level;
    }
}

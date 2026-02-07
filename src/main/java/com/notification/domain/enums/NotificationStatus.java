package com.notification.domain.enums;

/**
 * Status of notification delivery
 */
public enum NotificationStatus {
    PENDING("Pending", "Notification is queued"),
    PROCESSING("Processing", "Notification is being sent"),
    SENT("Sent", "Notification sent successfully"),
    FAILED("Failed", "Notification delivery failed"),
    SCHEDULED("Scheduled", "Notification scheduled for future"),
    CANCELLED("Cancelled", "Notification was cancelled"),
    RETRY("Retry", "Retrying failed notification");

    private final String displayName;
    private final String description;

    NotificationStatus(String displayName, String description) {
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

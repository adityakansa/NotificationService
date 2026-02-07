package com.notification.domain.enums;

/**
 * Enum representing different notification channels
 * Following Open/Closed Principle - new channels can be added without modifying existing code
 */
public enum NotificationChannel {
    EMAIL("Email", "email@example.com"),
    SMS("SMS", "+1234567890"),
    PUSH("Push Notification", "device-token"),
    WHATSAPP("WhatsApp", "+1234567890"),
    SLACK("Slack", "@username");

    private final String displayName;
    private final String sampleContact;

    NotificationChannel(String displayName, String sampleContact) {
        this.displayName = displayName;
        this.sampleContact = sampleContact;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSampleContact() {
        return sampleContact;
    }
}

package com.notification.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationChannel enum
 */
class NotificationChannelTest {

    @Test
    void testAllChannels() {
        assertEquals(5, NotificationChannel.values().length);
    }

    @Test
    void testEmailChannel() {
        assertEquals("Email", NotificationChannel.EMAIL.getDisplayName());
        assertEquals("email@example.com", NotificationChannel.EMAIL.getSampleContact());
    }

    @Test
    void testSmsChannel() {
        assertEquals("SMS", NotificationChannel.SMS.getDisplayName());
        assertEquals("+1234567890", NotificationChannel.SMS.getSampleContact());
    }

    @Test
    void testPushChannel() {
        assertEquals("Push Notification", NotificationChannel.PUSH.getDisplayName());
        assertEquals("device-token", NotificationChannel.PUSH.getSampleContact());
    }

    @Test
    void testWhatsAppChannel() {
        assertEquals("WhatsApp", NotificationChannel.WHATSAPP.getDisplayName());
        assertEquals("+1234567890", NotificationChannel.WHATSAPP.getSampleContact());
    }

    @Test
    void testSlackChannel() {
        assertEquals("Slack", NotificationChannel.SLACK.getDisplayName());
        assertEquals("@username", NotificationChannel.SLACK.getSampleContact());
    }

    @Test
    void testValueOf() {
        assertEquals(NotificationChannel.EMAIL, NotificationChannel.valueOf("EMAIL"));
        assertEquals(NotificationChannel.SMS, NotificationChannel.valueOf("SMS"));
        assertEquals(NotificationChannel.PUSH, NotificationChannel.valueOf("PUSH"));
    }
}

package com.notification.channel.impl;

import com.notification.channel.DeliveryResult;
import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Push Notification Channel Strategy
 */
class PushNotificationChannelStrategyTest {

    private PushNotificationChannelStrategy pushChannel;
    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        pushChannel = new PushNotificationChannelStrategy();
        
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .pushToken("fcm-token-12345-abcde-67890")
            .build();
        
        testNotification = Notification.builder()
            .id(1L)
            .user(testUser)
            .subject("Test Push")
            .body("Test push notification")
            .channel(NotificationChannel.PUSH)
            .priority(NotificationPriority.HIGH)
            .build();
    }

    @Test
    void testSend_ValidPushToken() {
        // Act
        DeliveryResult result = pushChannel.send(testNotification, testUser);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSend_NullPushToken_ReturnsFailure() {
        // Arrange
        testUser.setPushToken(null);
        
        // Act
        DeliveryResult result = pushChannel.send(testNotification, testUser);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Push token not found", result.getMessage());
        assertTrue(result.getErrorDetails().contains("no push token"));
    }

    @Test
    void testSend_EmptyPushToken_ReturnsFailure() {
        // Arrange
        testUser.setPushToken("");
        
        // Act
        DeliveryResult result = pushChannel.send(testNotification, testUser);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Push token not found", result.getMessage());
    }

    @Test
    void testCanDeliver_ValidPushToken_ReturnsTrue() {
        // Act
        boolean canDeliver = pushChannel.canDeliver(testUser);
        
        // Assert
        assertTrue(canDeliver);
    }

    @Test
    void testCanDeliver_NullPushToken_ReturnsFalse() {
        // Arrange
        testUser.setPushToken(null);
        
        // Act
        boolean canDeliver = pushChannel.canDeliver(testUser);
        
        // Assert
        assertFalse(canDeliver);
    }

    @Test
    void testCanDeliver_EmptyPushToken_ReturnsFalse() {
        // Arrange
        testUser.setPushToken("");
        
        // Act
        boolean canDeliver = pushChannel.canDeliver(testUser);
        
        // Assert
        assertFalse(canDeliver);
    }

    @Test
    void testCanDeliver_NullUser_ReturnsFalse() {
        // Act
        boolean canDeliver = pushChannel.canDeliver(null);
        
        // Assert
        assertFalse(canDeliver);
    }

    @Test
    void testGetChannelName() {
        // Act
        String channelName = pushChannel.getChannelName();
        
        // Assert
        assertEquals("PUSH", channelName);
    }

    @Test
    void testSupportsBatching() {
        // Act
        boolean supportsBatching = pushChannel.supportsBatching();
        
        // Assert
        assertTrue(supportsBatching);
    }
}

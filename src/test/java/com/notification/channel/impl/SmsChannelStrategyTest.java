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
 * Unit tests for SMS Channel Strategy
 */
class SmsChannelStrategyTest {

    private SmsChannelStrategy smsChannel;
    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        smsChannel = new SmsChannelStrategy();
        
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .phoneNumber("+1234567890")
            .build();
        
        testNotification = Notification.builder()
            .id(1L)
            .user(testUser)
            .subject("Test SMS")
            .body("Test SMS message")
            .channel(NotificationChannel.SMS)
            .priority(NotificationPriority.MEDIUM)
            .build();
    }

    @Test
    void testSend_ValidPhoneNumber() {
        // Act
        DeliveryResult result = smsChannel.send(testNotification, testUser);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSend_NullPhoneNumber_ReturnsFailure() {
        // Arrange
        testUser.setPhoneNumber(null);
        
        // Act
        DeliveryResult result = smsChannel.send(testNotification, testUser);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Phone number not found", result.getMessage());
        assertTrue(result.getErrorDetails().contains("no phone number"));
    }

    @Test
    void testSend_EmptyPhoneNumber_ReturnsFailure() {
        // Arrange
        testUser.setPhoneNumber("");
        
        // Act
        DeliveryResult result = smsChannel.send(testNotification, testUser);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Phone number not found", result.getMessage());
    }

    @Test
    void testCanDeliver_ValidPhoneNumber_ReturnsTrue() {
        // Act
        boolean canDeliver = smsChannel.canDeliver(testUser);
        
        // Assert
        assertTrue(canDeliver);
    }

    @Test
    void testCanDeliver_NullPhoneNumber_ReturnsFalse() {
        // Arrange
        testUser.setPhoneNumber(null);
        
        // Act
        boolean canDeliver = smsChannel.canDeliver(testUser);
        
        // Assert
        assertFalse(canDeliver);
    }

    @Test
    void testCanDeliver_EmptyPhoneNumber_ReturnsFalse() {
        // Arrange
        testUser.setPhoneNumber("");
        
        // Act
        boolean canDeliver = smsChannel.canDeliver(testUser);
        
        // Assert
        assertFalse(canDeliver);
    }

    @Test
    void testCanDeliver_InvalidPhoneNumber_ReturnsFalse() {
        // Arrange
        testUser.setPhoneNumber("invalid");
        
        // Act
        boolean canDeliver = smsChannel.canDeliver(testUser);
        
        // Assert
        assertFalse(canDeliver);
    }

    @Test
    void testCanDeliver_NullUser_ReturnsFalse() {
        // Act
        boolean canDeliver = smsChannel.canDeliver(null);
        
        // Assert
        assertFalse(canDeliver);
    }

    @Test
    void testGetChannelName() {
        // Act
        String channelName = smsChannel.getChannelName();
        
        // Assert
        assertEquals("SMS", channelName);
    }

    @Test
    void testSupportsBatching() {
        // Act
        boolean supportsBatching = smsChannel.supportsBatching();
        
        // Assert
        assertTrue(supportsBatching);
    }
}

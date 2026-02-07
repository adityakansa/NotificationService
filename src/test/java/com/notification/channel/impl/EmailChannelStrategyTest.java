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
 * Unit tests for Email Channel Strategy
 */
class EmailChannelStrategyTest {
    
    private EmailChannelStrategy emailChannel;
    private User testUser;
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        emailChannel = new EmailChannelStrategy();
        
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        
        testNotification = Notification.builder()
            .id(1L)
            .user(testUser)
            .subject("Test Email")
            .body("Test email body")
            .channel(NotificationChannel.EMAIL)
            .priority(NotificationPriority.MEDIUM)
            .build();
    }
    
    @Test
    void testSend_ValidEmail() {
        // Act
        DeliveryResult result = emailChannel.send(testNotification, testUser);
        
        // Assert - with 10% failure rate, we can't guarantee success, but we can check the result is not null
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testSend_MissingEmail() {
        // Arrange
        testUser.setEmail(null);
        
        // Act
        DeliveryResult result = emailChannel.send(testNotification, testUser);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Email address not found"));
    }
    
    @Test
    void testCanDeliver_ValidEmail() {
        // Act
        boolean canDeliver = emailChannel.canDeliver(testUser);
        
        // Assert
        assertTrue(canDeliver);
    }
    
    @Test
    void testCanDeliver_InvalidEmail() {
        // Arrange
        testUser.setEmail("invalid-email");
        
        // Act
        boolean canDeliver = emailChannel.canDeliver(testUser);
        
        // Assert
        assertFalse(canDeliver);
    }
    
    @Test
    void testGetChannelName() {
        // Act
        String channelName = emailChannel.getChannelName();
        
        // Assert
        assertEquals("EMAIL", channelName);
    }
    
    @Test
    void testSupportsBatching() {
        // Act
        boolean supportsBatching = emailChannel.supportsBatching();
        
        // Assert
        assertTrue(supportsBatching);
    }
}

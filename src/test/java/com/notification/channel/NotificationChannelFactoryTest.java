package com.notification.channel;

import com.notification.channel.impl.EmailChannelStrategy;
import com.notification.channel.impl.PushNotificationChannelStrategy;
import com.notification.channel.impl.SmsChannelStrategy;
import com.notification.domain.enums.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotificationChannelFactory
 */
class NotificationChannelFactoryTest {

    private NotificationChannelFactory factory;
    private List<NotificationChannelStrategy> strategies;

    @BeforeEach
    void setUp() {
        // Create real strategy instances for testing
        strategies = List.of(
            new EmailChannelStrategy(),
            new SmsChannelStrategy(),
            new PushNotificationChannelStrategy()
        );
        
        factory = new NotificationChannelFactory(strategies);
    }

    @Test
    void testGetStrategy_Email_Success() {
        // Act
        NotificationChannelStrategy strategy = factory.getStrategy(NotificationChannel.EMAIL);

        // Assert
        assertNotNull(strategy);
        assertEquals("EMAIL", strategy.getChannelName());
        assertInstanceOf(EmailChannelStrategy.class, strategy);
    }

    @Test
    void testGetStrategy_SMS_Success() {
        // Act
        NotificationChannelStrategy strategy = factory.getStrategy(NotificationChannel.SMS);

        // Assert
        assertNotNull(strategy);
        assertEquals("SMS", strategy.getChannelName());
        assertInstanceOf(SmsChannelStrategy.class, strategy);
    }

    @Test
    void testGetStrategy_Push_Success() {
        // Act
        NotificationChannelStrategy strategy = factory.getStrategy(NotificationChannel.PUSH);

        // Assert
        assertNotNull(strategy);
        assertEquals("PUSH", strategy.getChannelName());
        assertInstanceOf(PushNotificationChannelStrategy.class, strategy);
    }

    @Test
    void testGetStrategy_UnsupportedChannel_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.getStrategy(NotificationChannel.WHATSAPP)
        );
        
        assertTrue(exception.getMessage().contains("No strategy found for channel"));
        assertTrue(exception.getMessage().contains("WHATSAPP"));
    }

    @Test
    void testIsChannelSupported_Email_ReturnsTrue() {
        // Act
        boolean supported = factory.isChannelSupported(NotificationChannel.EMAIL);

        // Assert
        assertTrue(supported);
    }

    @Test
    void testIsChannelSupported_SMS_ReturnsTrue() {
        // Act
        boolean supported = factory.isChannelSupported(NotificationChannel.SMS);

        // Assert
        assertTrue(supported);
    }

    @Test
    void testIsChannelSupported_Push_ReturnsTrue() {
        // Act
        boolean supported = factory.isChannelSupported(NotificationChannel.PUSH);

        // Assert
        assertTrue(supported);
    }

    @Test
    void testIsChannelSupported_WhatsApp_ReturnsFalse() {
        // Act
        boolean supported = factory.isChannelSupported(NotificationChannel.WHATSAPP);

        // Assert
        assertFalse(supported);
    }

    @Test
    void testIsChannelSupported_Slack_ReturnsFalse() {
        // Act
        boolean supported = factory.isChannelSupported(NotificationChannel.SLACK);

        // Assert
        assertFalse(supported);
    }

    @Test
    void testGetAllStrategies_ReturnsCorrectSize() {
        // Act
        Map<NotificationChannel, NotificationChannelStrategy> allStrategies = factory.getAllStrategies();

        // Assert
        assertNotNull(allStrategies);
        assertEquals(3, allStrategies.size());
        assertTrue(allStrategies.containsKey(NotificationChannel.EMAIL));
        assertTrue(allStrategies.containsKey(NotificationChannel.SMS));
        assertTrue(allStrategies.containsKey(NotificationChannel.PUSH));
    }

    @Test
    void testGetAllStrategies_ReturnsNewMap() {
        // Act
        Map<NotificationChannel, NotificationChannelStrategy> allStrategies1 = factory.getAllStrategies();
        Map<NotificationChannel, NotificationChannelStrategy> allStrategies2 = factory.getAllStrategies();

        // Assert - should be different instances
        assertNotSame(allStrategies1, allStrategies2);
        assertEquals(allStrategies1.size(), allStrategies2.size());
    }

    @Test
    void testFactoryInitialization_WithEmptyStrategies() {
        // Arrange
        NotificationChannelFactory emptyFactory = new NotificationChannelFactory(List.of());

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> emptyFactory.getStrategy(NotificationChannel.EMAIL)
        );
    }
}

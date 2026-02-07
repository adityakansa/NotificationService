package com.notification.channel;

import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;

/**
 * Strategy Interface for Notification Channels
 * Follows Strategy Pattern and Open/Closed Principle
 * New channels can be added by implementing this interface without modifying existing code
 */
public interface NotificationChannelStrategy {
    
    /**
     * Send notification through the channel
     * 
     * @param notification The notification to send
     * @param user The recipient user
     * @return DeliveryResult containing status and details
     */
    DeliveryResult send(Notification notification, User user);
    
    /**
     * Validate if the channel can deliver to the user
     * 
     * @param user The user to validate
     * @return true if channel can deliver, false otherwise
     */
    boolean canDeliver(User user);
    
    /**
     * Get the channel name
     * 
     * @return Channel identifier
     */
    String getChannelName();
    
    /**
     * Check if channel supports batching
     * 
     * @return true if batching is supported
     */
    default boolean supportsBatching() {
        return false;
    }
}

package com.notification.channel;

import com.notification.domain.enums.NotificationChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating appropriate channel strategies
 * Follows Factory Pattern and Dependency Inversion Principle
 * Centralizes channel strategy creation and management
 */
@Component
public class NotificationChannelFactory {
    
    private final Map<NotificationChannel, NotificationChannelStrategy> channelStrategies;
    
    @Autowired
    public NotificationChannelFactory(List<NotificationChannelStrategy> strategies) {
        this.channelStrategies = new HashMap<>();
        
        // Register all available strategies
        for (NotificationChannelStrategy strategy : strategies) {
            NotificationChannel channel = NotificationChannel.valueOf(strategy.getChannelName());
            channelStrategies.put(channel, strategy);
        }
    }
    
    /**
     * Get the appropriate strategy for a given channel
     * 
     * @param channel The notification channel
     * @return The corresponding strategy
     * @throws IllegalArgumentException if channel not supported
     */
    public NotificationChannelStrategy getStrategy(NotificationChannel channel) {
        NotificationChannelStrategy strategy = channelStrategies.get(channel);
        
        if (strategy == null) {
            throw new IllegalArgumentException(
                String.format("%s channel strategy is not yet implemented. " +
                             "Available channels: EMAIL, SMS, PUSH", channel)
            );
        }
        
        return strategy;
    }
    
    /**
     * Check if a channel is supported
     * 
     * @param channel The notification channel
     * @return true if channel is supported
     */
    public boolean isChannelSupported(NotificationChannel channel) {
        return channelStrategies.containsKey(channel);
    }
    
    /**
     * Get all supported channels
     * 
     * @return Map of supported channels and their strategies
     */
    public Map<NotificationChannel, NotificationChannelStrategy> getAllStrategies() {
        return new HashMap<>(channelStrategies);
    }
}

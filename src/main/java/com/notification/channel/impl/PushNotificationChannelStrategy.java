package com.notification.channel.impl;

import com.notification.channel.DeliveryResult;
import com.notification.channel.NotificationChannelStrategy;
import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Push Notification Channel Implementation
 * Real-time notifications to mobile devices
 */
@Slf4j
@Component("pushChannel")
public class PushNotificationChannelStrategy implements NotificationChannelStrategy {
    
    @Override
    public DeliveryResult send(Notification notification, User user) {
        try {
            String pushToken = user.getPushToken();
            
            if (pushToken == null || pushToken.isEmpty()) {
                return DeliveryResult.failure("Push token not found", "User has no push token configured");
            }
            
            // Simulate push notification sending (in real implementation, use FCM or APNs)
            log.info("🔔 Sending PUSH notification to token: {}...", 
                     pushToken.substring(0, Math.min(20, pushToken.length())));
            log.info("   Title: {}", notification.getSubject());
            log.info("   Body: {}", notification.getBody());
            log.info("   Priority: {}", notification.getPriority());
            
            // Simulate network delay
            Thread.sleep(80);
            
            // Simulate occasional failures for demo purposes (3% failure rate)
            if (Math.random() < 0.03) {
                throw new RuntimeException("Push notification service unavailable");
            }
            
            log.info("✅ Push notification sent successfully");
            
            return DeliveryResult.success(
                String.format("Push notification sent successfully to device")
            );
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DeliveryResult.failure("Push notification sending interrupted", e);
        } catch (Exception e) {
            log.error("❌ Failed to send push notification: {}", e.getMessage());
            return DeliveryResult.failure("Failed to send push notification", e);
        }
    }
    
    @Override
    public boolean canDeliver(User user) {
        return user != null && 
               user.getPushToken() != null && 
               !user.getPushToken().isEmpty();
    }
    
    @Override
    public String getChannelName() {
        return "PUSH";
    }
    
    @Override
    public boolean supportsBatching() {
        return true; // Push notifications support bulk sending
    }
}

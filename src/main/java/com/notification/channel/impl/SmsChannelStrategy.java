package com.notification.channel.impl;

import com.notification.channel.DeliveryResult;
import com.notification.channel.NotificationChannelStrategy;
import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SMS Channel Implementation
 * Demonstrates extensibility - new channel added without modifying existing code
 */
@Slf4j
@Component("smsChannel")
public class SmsChannelStrategy implements NotificationChannelStrategy {
    
    @Override
    public DeliveryResult send(Notification notification, User user) {
        try {
            String phoneNumber = user.getPhoneNumber();
            
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return DeliveryResult.failure("Phone number not found", "User has no phone number configured");
            }
            
            // Simulate SMS sending (in real implementation, use Twilio or similar service)
            log.info("📱 Sending SMS to: {}", phoneNumber);
            log.info("   Message: {}", notification.getBody());
            log.info("   Priority: {}", notification.getPriority());
            
            // Simulate network delay
            Thread.sleep(150);
            
            // Simulate occasional failures for demo purposes (5% failure rate)
            if (Math.random() < 0.05) {
                throw new RuntimeException("SMS gateway timeout");
            }
            
            log.info("✅ SMS sent successfully to {}", phoneNumber);
            
            return DeliveryResult.success(
                String.format("SMS sent successfully to %s", phoneNumber)
            );
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DeliveryResult.failure("SMS sending interrupted", e);
        } catch (Exception e) {
            log.error("❌ Failed to send SMS: {}", e.getMessage());
            return DeliveryResult.failure("Failed to send SMS", e);
        }
    }
    
    @Override
    public boolean canDeliver(User user) {
        return user != null && 
               user.getPhoneNumber() != null && 
               !user.getPhoneNumber().isEmpty() &&
               user.getPhoneNumber().matches("^\\+?[0-9]{10,15}$");
    }
    
    @Override
    public String getChannelName() {
        return "SMS";
    }
    
    @Override
    public boolean supportsBatching() {
        return true; // SMS supports bulk sending
    }
}

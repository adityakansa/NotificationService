package com.notification.channel.impl;

import com.notification.channel.DeliveryResult;
import com.notification.channel.NotificationChannelStrategy;
import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Email Channel Implementation
 * Follows Single Responsibility Principle - handles only email delivery
 */
@Slf4j
@Component("emailChannel")
public class EmailChannelStrategy implements NotificationChannelStrategy {
    
    @Override
    public DeliveryResult send(Notification notification, User user) {
        try {
            String email = user.getEmail();
            
            if (email == null || email.isEmpty()) {
                return DeliveryResult.failure("Email address not found", "User has no email configured");
            }
            
            // Simulate email sending (in real implementation, use JavaMailSender or email service)
            log.info("📧 Sending EMAIL to: {}", email);
            log.info("   Subject: {}", notification.getSubject());
            log.info("   Body: {}", notification.getBody());
            log.info("   Priority: {}", notification.getPriority());
            
            // Simulate network delay
            Thread.sleep(100);
            
            // Simulate occasional failures for demo purposes (10% failure rate)
            if (Math.random() < 0.1) {
                throw new RuntimeException("SMTP server temporarily unavailable");
            }
            
            log.info("✅ Email sent successfully to {}", email);
            
            return DeliveryResult.success(
                String.format("Email sent successfully to %s", email)
            );
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DeliveryResult.failure("Email sending interrupted", e);
        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage());
            return DeliveryResult.failure("Failed to send email", e);
        }
    }
    
    @Override
    public boolean canDeliver(User user) {
        return user != null && 
               user.getEmail() != null && 
               !user.getEmail().isEmpty() &&
               user.getEmail().contains("@");
    }
    
    @Override
    public String getChannelName() {
        return "EMAIL";
    }
    
    @Override
    public boolean supportsBatching() {
        return true; // Email supports bulk sending
    }
}

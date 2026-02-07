package com.notification.service;

import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * User Management Service
 * Handles user registration and preferences
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Register a new user
     */
    @Transactional
    public User registerUser(User user) {
        log.info("Registering new user: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());
        
        return user;
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
    
    /**
     * Update user channel preferences
     */
    @Transactional
    public User updateChannelPreferences(Long userId, Set<NotificationChannel> channels) {
        log.info("Updating channel preferences for user: {}", userId);
        
        User user = getUser(userId);
        user.setPreferredChannels(channels);
        
        user = userRepository.save(user);
        log.info("Channel preferences updated for user: {}", userId);
        
        return user;
    }
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Deactivate user
     */
    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);
        
        User user = getUser(userId);
        user.setActive(false);
        userRepository.save(user);
        
        log.info("User deactivated: {}", userId);
    }
    
    /**
     * Update contact information for a specific channel
     */
    @Transactional
    public User updateChannelContact(Long userId, NotificationChannel channel, String contactInfo) {
        log.info("Updating {} channel contact for user: {}", channel, userId);
        
        User user = getUser(userId);
        
        // Update the appropriate field based on channel
        switch (channel) {
            case EMAIL -> user.setEmail(contactInfo);
            case SMS -> user.setPhoneNumber(contactInfo);
            case PUSH -> user.setPushToken(contactInfo);
            case WHATSAPP -> user.setWhatsappNumber(contactInfo);
            case SLACK -> user.setSlackHandle(contactInfo);
        }
        
        user = userRepository.save(user);
        log.info("{} channel contact updated for user: {}", channel, userId);
        
        return user;
    }
    
    /**
     * Enable a specific channel for user
     */
    @Transactional
    public User enableChannel(Long userId, NotificationChannel channel) {
        log.info("Enabling {} channel for user: {}", channel, userId);
        
        User user = getUser(userId);
        
        // Check if contact info exists for the channel
        if (user.getContactForChannel(channel) == null) {
            throw new IllegalStateException(
                String.format("Cannot enable %s channel. Contact information not set.", channel.getDisplayName())
            );
        }
        
        user.getPreferredChannels().add(channel);
        user = userRepository.save(user);
        
        log.info("{} channel enabled for user: {}", channel, userId);
        return user;
    }
    
    /**
     * Disable a specific channel for user
     */
    @Transactional
    public User disableChannel(Long userId, NotificationChannel channel) {
        log.info("Disabling {} channel for user: {}", channel, userId);
        
        User user = getUser(userId);
        user.getPreferredChannels().remove(channel);
        user = userRepository.save(user);
        
        log.info("{} channel disabled for user: {}", channel, userId);
        return user;
    }
}

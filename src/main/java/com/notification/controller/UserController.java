package com.notification.controller;

import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST Controller for User Management
 * Provides APIs for user registration and preference management
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Register a new user
     */
    @PostMapping
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        log.info("REST API: Register user - {}", user.getUsername());
        User registered = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        log.info("REST API: Get user - {}", userId);
        User user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("REST API: Get all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Update user channel preferences
     */
    @PutMapping("/{userId}/preferences")
    public ResponseEntity<User> updatePreferences(
            @PathVariable Long userId,
            @RequestBody Set<NotificationChannel> channels) {
        log.info("REST API: Update preferences for user {} - {}", userId, channels);
        User updated = userService.updateChannelPreferences(userId, channels);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Update user contact information for a specific channel
     */
    @PutMapping("/{userId}/channels/{channel}")
    public ResponseEntity<User> updateChannelContact(
            @PathVariable Long userId,
            @PathVariable NotificationChannel channel,
            @RequestBody java.util.Map<String, String> payload) {
        log.info("REST API: Update {} channel contact for user {}", channel, userId);
        String contactInfo = payload.get("contact");
        User updated = userService.updateChannelContact(userId, channel, contactInfo);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Enable a specific channel for user
     */
    @PatchMapping("/{userId}/channels/{channel}")
    public ResponseEntity<User> enableChannel(
            @PathVariable Long userId,
            @PathVariable NotificationChannel channel) {
        log.info("REST API: Enable {} channel for user {}", channel, userId);
        User updated = userService.enableChannel(userId, channel);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Disable a specific channel for user
     */
    @DeleteMapping("/{userId}/channels/{channel}")
    public ResponseEntity<User> disableChannel(
            @PathVariable Long userId,
            @PathVariable NotificationChannel channel) {
        log.info("REST API: Disable {} channel for user {}", channel, userId);
        User updated = userService.disableChannel(userId, channel);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Deactivate user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        log.info("REST API: Deactivate user - {}", userId);
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }
}

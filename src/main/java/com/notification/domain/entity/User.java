package com.notification.domain.entity;

import com.notification.domain.enums.NotificationChannel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User Entity - Represents a user who can receive notifications
 * Follows Single Responsibility Principle - manages user data only
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    @Column(nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false)
    private String email;
    
    private String phoneNumber;
    private String pushToken;
    private String whatsappNumber;
    private String slackHandle;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    
    // User preferences for notification channels
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_channel_preferences", 
                     joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<NotificationChannel> preferredChannels = new HashSet<>();
    
    // User-specific variables for personalization
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_variables", 
                     joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "variable_key")
    @Column(name = "variable_value")
    @Builder.Default
    private Map<String, String> personalizationVariables = new HashMap<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get contact information for specific channel
     */
    public String getContactForChannel(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> email;
            case SMS -> phoneNumber;
            case PUSH -> pushToken;
            case WHATSAPP -> whatsappNumber;
            case SLACK -> slackHandle;
        };
    }
    
    /**
     * Check if user can receive notifications on given channel
     */
    public boolean canReceiveOnChannel(NotificationChannel channel) {
        return active && 
               preferredChannels.contains(channel) && 
               getContactForChannel(channel) != null;
    }
}

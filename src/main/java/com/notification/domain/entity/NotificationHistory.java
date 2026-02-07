package com.notification.domain.entity;

import com.notification.domain.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification History Entity - Audit trail for notifications
 * Follows Single Responsibility Principle - tracks notification delivery history
 */
@Entity
@Table(name = "notification_history", indexes = {
    @Index(name = "idx_notification_id", columnList = "notification_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;
    
    @Column(name = "attempt_number")
    private Integer attemptNumber;
    
    @Column(length = 1000)
    private String message;
    
    @Column(name = "error_details", length = 2000)
    private String errorDetails;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.notification.repository;

import com.notification.domain.entity.NotificationHistory;
import com.notification.domain.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for NotificationHistory entity
 * Tracks audit trail of notification attempts
 */
@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
    
    List<NotificationHistory> findByNotificationIdOrderByCreatedAtDesc(Long notificationId);
    
    @Query("SELECT nh FROM NotificationHistory nh WHERE nh.notificationId = :notificationId " +
           "AND nh.status = :status ORDER BY nh.createdAt DESC")
    List<NotificationHistory> findByNotificationIdAndStatus(
        @Param("notificationId") Long notificationId,
        @Param("status") NotificationStatus status
    );
    
    @Query("SELECT nh FROM NotificationHistory nh WHERE nh.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY nh.createdAt DESC")
    List<NotificationHistory> findByCreatedAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT COUNT(nh) FROM NotificationHistory nh WHERE nh.status = :status " +
           "AND nh.createdAt >= :since")
    Long countByStatusSince(
        @Param("status") NotificationStatus status,
        @Param("since") LocalDateTime since
    );
}

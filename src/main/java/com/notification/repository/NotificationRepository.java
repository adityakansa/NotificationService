package com.notification.repository;

import com.notification.domain.entity.Notification;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import com.notification.domain.enums.ScheduleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity
 * Provides custom queries for notification management
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    
    List<Notification> findByStatus(NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status ORDER BY n.priority ASC, n.createdAt ASC")
    List<Notification> findByStatusOrderByPriorityAndCreatedAt(@Param("status") NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.priority = :priority")
    List<Notification> findByStatusAndPriority(
        @Param("status") NotificationStatus status, 
        @Param("priority") NotificationPriority priority
    );
    
    @Query("SELECT n FROM Notification n WHERE n.scheduleType = :scheduleType " +
           "AND n.scheduledTime <= :now AND n.status = :status")
    List<Notification> findScheduledNotificationsDue(
        @Param("scheduleType") ScheduleType scheduleType,
        @Param("now") LocalDateTime now,
        @Param("status") NotificationStatus status
    );
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status " +
           "AND n.nextRetryTime <= :now AND n.currentRetryAttempt < n.maxRetryAttempts")
    List<Notification> findNotificationsForRetry(
        @Param("status") NotificationStatus status,
        @Param("now") LocalDateTime now
    );
    
    @Query("SELECT n FROM Notification n WHERE n.scheduleType = 'RECURRING' " +
           "AND n.status = 'SENT' " +
           "AND (n.maxOccurrences IS NULL OR n.occurrenceCount < n.maxOccurrences) " +
           "AND (n.recurrenceEndTime IS NULL OR n.recurrenceEndTime > CURRENT_TIMESTAMP)")
    List<Notification> findRecurringNotificationsForNextExecution();
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    List<Notification> findByUserId(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.priority = :priority " +
           "ORDER BY n.createdAt ASC")
    List<Notification> findByStatusAndPriorityOrderByCreatedAtAsc(
        @Param("status") NotificationStatus status,
        @Param("priority") NotificationPriority priority
    );
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    Long countByStatus(@Param("status") NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.status IN :statuses " +
           "ORDER BY n.priority ASC, n.createdAt ASC")
    List<Notification> findByStatusInOrderByPriority(@Param("statuses") List<NotificationStatus> statuses);
}

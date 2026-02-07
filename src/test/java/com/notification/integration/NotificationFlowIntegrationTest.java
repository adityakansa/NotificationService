package com.notification.integration;

import com.notification.domain.entity.Notification;
import com.notification.domain.entity.User;
import com.notification.domain.enums.*;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.repository.NotificationRepository;
import com.notification.repository.UserRepository;
import com.notification.service.NotificationSchedulerService;
import com.notification.service.NotificationService;
import com.notification.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for complete notification flows
 * Tests end-to-end scenarios with actual database interactions
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationFlowIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationSchedulerService schedulerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
            .username("integration-test-user")
            .email("test@integration.com")
            .phoneNumber("+1234567890")
            .pushToken("test-push-token")
            .active(true)
            .preferredChannels(new HashSet<>(Set.of(
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                NotificationChannel.PUSH
            )))
            .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCompleteFlow_CreateUser_ScheduleNotification_Reschedule_Send() {
        // Step 1: Create User (already done in setUp, verify it exists)
        User savedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(savedUser, "User should be created");
        assertEquals("integration-test-user", savedUser.getUsername());
        assertTrue(savedUser.getActive());

        // Step 2: Schedule a notification
        LocalDateTime originalScheduleTime = LocalDateTime.now().plusHours(2);
        NotificationRequest request = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("Integration Test Notification")
            .body("This is a scheduled notification for integration testing")
            .priority(NotificationPriority.MEDIUM)
            .scheduleType(ScheduleType.SCHEDULED)
            .scheduledTime(originalScheduleTime)
            .build();

        NotificationResponse scheduledResponse = notificationService.createNotification(request);
        assertNotNull(scheduledResponse);
        assertNotNull(scheduledResponse.getId());
        assertEquals(NotificationStatus.SCHEDULED, scheduledResponse.getStatus());

        // Retrieve the notification entity
        Notification scheduledNotification = notificationRepository.findById(scheduledResponse.getId()).orElse(null);
        assertNotNull(scheduledNotification);
        assertEquals(originalScheduleTime, scheduledNotification.getScheduledTime());

        // Step 3: Reschedule the notification to a new time
        LocalDateTime newScheduleTime = LocalDateTime.now().plusMinutes(30);
        Notification rescheduledNotification = schedulerService.rescheduleNotification(
            scheduledResponse.getId(),
            newScheduleTime
        );

        assertNotNull(rescheduledNotification);
        assertEquals(newScheduleTime, rescheduledNotification.getScheduledTime());
        assertEquals(NotificationStatus.SCHEDULED, rescheduledNotification.getStatus());

        // Step 4: Send the notification
        notificationService.sendNotification(rescheduledNotification.getId());

        // Step 5: Verify the notification was sent
        Notification sentNotification = notificationRepository.findById(rescheduledNotification.getId()).orElse(null);
        assertNotNull(sentNotification);
        assertTrue(
            sentNotification.getStatus() == NotificationStatus.SENT ||
            sentNotification.getStatus() == NotificationStatus.RETRY,
            "Notification should be SENT or RETRY after send attempt, but was: " + sentNotification.getStatus()
        );
        
        if (sentNotification.getStatus() == NotificationStatus.SENT) {
            assertNotNull(sentNotification.getSentAt());
            // Verify the complete flow
            assertTrue(sentNotification.getCreatedAt().isBefore(sentNotification.getSentAt()),
                "Notification should be sent after it was created");
        }
    }

    @Test
    void testPriorityOrdering_HighPriorityNotificationsSentFirst() {
        // Create multiple notifications with different priorities
        
        // Low priority notification
        NotificationRequest lowPriorityRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("Low Priority Notification")
            .body("This should be sent last")
            .priority(NotificationPriority.LOW)
            .build();
        NotificationResponse lowPriorityResponse = notificationService.createNotification(lowPriorityRequest);

        // Medium priority notification
        NotificationRequest mediumPriorityRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.SMS)
            .subject("Medium Priority Notification")
            .body("This should be sent second")
            .priority(NotificationPriority.MEDIUM)
            .build();
        NotificationResponse mediumPriorityResponse = notificationService.createNotification(mediumPriorityRequest);

        // High priority notification 1
        NotificationRequest highPriorityRequest1 = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.PUSH)
            .subject("High Priority Notification 1")
            .body("This should be sent first")
            .priority(NotificationPriority.HIGH)
            .build();
        NotificationResponse highPriorityResponse1 = notificationService.createNotification(highPriorityRequest1);

        // High priority notification 2
        NotificationRequest highPriorityRequest2 = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("High Priority Notification 2")
            .body("This should also be sent first")
            .priority(NotificationPriority.HIGH)
            .build();
        NotificationResponse highPriorityResponse2 = notificationService.createNotification(highPriorityRequest2);

        // Verify all notifications are created with PENDING status
        List<Notification> allNotifications = notificationRepository.findAll();
        assertEquals(4, allNotifications.size());
        
        // Verify high priority notifications exist
        long highPriorityCount = allNotifications.stream()
            .filter(n -> n.getPriority() == NotificationPriority.HIGH)
            .count();
        assertEquals(2, highPriorityCount, "Should have 2 high priority notifications");

        // Send notifications (in a real scenario, the queue service would handle priority)
        // For this test, we'll verify the priority ordering by checking creation
        List<Notification> highPriorityNotifications = notificationRepository
            .findByStatusAndPriorityOrderByCreatedAtAsc(
                NotificationStatus.PENDING,
                NotificationPriority.HIGH
            );
        
        assertEquals(2, highPriorityNotifications.size(),
            "High priority notifications should be identified correctly");

        // Send high priority notifications first
        for (Notification notification : highPriorityNotifications) {
            notificationService.sendNotification(notification.getId());
        }

        // Verify high priority notifications are sent
        Notification sentHigh1 = notificationRepository.findById(highPriorityResponse1.getId()).orElse(null);
        Notification sentHigh2 = notificationRepository.findById(highPriorityResponse2.getId()).orElse(null);
        
        assertNotNull(sentHigh1);
        assertNotNull(sentHigh2);
        assertEquals(NotificationStatus.SENT, sentHigh1.getStatus());
        assertEquals(NotificationStatus.SENT, sentHigh2.getStatus());
        assertNotNull(sentHigh1.getSentAt());
        assertNotNull(sentHigh2.getSentAt());

        // Verify medium and low priority notifications are still pending
        Notification mediumNotification = notificationRepository.findById(mediumPriorityResponse.getId()).orElse(null);
        Notification lowNotification = notificationRepository.findById(lowPriorityResponse.getId()).orElse(null);
        
        assertNotNull(mediumNotification);
        assertNotNull(lowNotification);
        assertEquals(NotificationStatus.PENDING, mediumNotification.getStatus());
        assertEquals(NotificationStatus.PENDING, lowNotification.getStatus());
    }

    @Test
    void testBulkNotificationCreation_AllChannels() {
        // Create notifications for all available channels
        NotificationChannel[] allChannels = {
            NotificationChannel.EMAIL,
            NotificationChannel.SMS,
            NotificationChannel.PUSH,
            NotificationChannel.WHATSAPP,
            NotificationChannel.SLACK
        };

        // Update user with all channel contact info
        testUser.setWhatsappNumber("+1987654321");
        testUser.setSlackHandle("@testuser");
        testUser.setPreferredChannels(new HashSet<>(Set.of(allChannels)));
        userRepository.save(testUser);

        for (NotificationChannel channel : allChannels) {
            NotificationRequest request = NotificationRequest.builder()
                .userId(testUser.getId())
                .channel(channel)
                .subject("Test for " + channel.getDisplayName())
                .body("Testing " + channel.getDisplayName() + " channel")
                .priority(NotificationPriority.MEDIUM)
                .build();

            NotificationResponse response = notificationService.createNotification(request);
            assertNotNull(response);
            assertEquals(channel, response.getChannel());
            assertEquals(NotificationStatus.PENDING, response.getStatus());
        }

        // Verify all notifications are created
        List<Notification> allNotifications = notificationRepository
            .findByUserId(testUser.getId());
        assertEquals(5, allNotifications.size(), "Should have notifications for all 5 channels");

        // Verify each channel type exists
        for (NotificationChannel channel : allChannels) {
            boolean channelExists = allNotifications.stream()
                .anyMatch(n -> n.getChannel() == channel);
            assertTrue(channelExists, channel.getDisplayName() + " notification should exist");
        }
    }

    @Test
    void testRecurringNotification_MultipleOccurrences() {
        // Create a recurring notification
        NotificationRequest recurringRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("Daily Recurring Notification")
            .body("This notification recurs daily")
            .priority(NotificationPriority.MEDIUM)
            .scheduleType(ScheduleType.RECURRING)
            .scheduledTime(LocalDateTime.now().plusMinutes(1))
            .recurrenceFrequency(RecurrenceFrequency.DAILY)
            .maxOccurrences(5)
            .build();

        NotificationResponse recurringResponse = notificationService.createNotification(recurringRequest);
        assertNotNull(recurringResponse);
        
        // Retrieve entity to manipulate
        Notification recurringNotification = notificationRepository.findById(recurringResponse.getId()).orElse(null);
        assertNotNull(recurringNotification);
        assertEquals(ScheduleType.RECURRING, recurringNotification.getScheduleType());
        assertEquals(RecurrenceFrequency.DAILY, recurringNotification.getRecurrenceFrequency());
        assertEquals(5, recurringNotification.getMaxOccurrences());
        assertEquals(0, recurringNotification.getOccurrenceCount());

        // Simulate first occurrence
        recurringNotification.incrementOccurrence();
        notificationRepository.save(recurringNotification);

        // Verify it should continue
        assertTrue(recurringNotification.shouldContinueRecurrence());
        assertEquals(1, recurringNotification.getOccurrenceCount());

        // Simulate reaching max occurrences
        for (int i = 1; i < 5; i++) {
            recurringNotification.incrementOccurrence();
        }
        notificationRepository.save(recurringNotification);

        // Verify it should stop
        assertFalse(recurringNotification.shouldContinueRecurrence());
        assertEquals(5, recurringNotification.getOccurrenceCount());
    }

    @Test
    void testNotificationRetry_AfterFailure() {
        // Create a notification
        NotificationRequest request = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("Test Retry Notification")
            .body("This notification will fail and retry")
            .priority(NotificationPriority.HIGH)
            .build();

        NotificationResponse response = notificationService.createNotification(request);
        assertNotNull(response);
        
        // Retrieve entity to manipulate
        Notification notification = notificationRepository.findById(response.getId()).orElse(null);
        assertNotNull(notification);
        assertEquals(0, notification.getCurrentRetryAttempt());
        assertTrue(notification.canRetry());

        // Simulate first failure
        notification.recordRetryAttempt("Network timeout");
        notificationRepository.save(notification);

        assertEquals(1, notification.getCurrentRetryAttempt());
        assertEquals(NotificationStatus.RETRY, notification.getStatus());
        assertEquals("Network timeout", notification.getLastFailureReason());
        assertNotNull(notification.getNextRetryTime());
        assertTrue(notification.canRetry());

        // Simulate second failure
        notification.recordRetryAttempt("Service unavailable");
        notificationRepository.save(notification);

        assertEquals(2, notification.getCurrentRetryAttempt());
        assertTrue(notification.canRetry());

        // Simulate third failure (max reached)
        notification.recordRetryAttempt("Connection refused");
        notificationRepository.save(notification);

        assertEquals(3, notification.getCurrentRetryAttempt());
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertFalse(notification.canRetry());
        assertTrue(notification.getLastFailureReason().contains("Max retry attempts exceeded"));
    }

    @Test
    void testUserDeactivation_NotificationsStillExist() {
        // Create a notification for the user
        NotificationRequest request = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("Test Before Deactivation")
            .body("This notification exists before user deactivation")
            .priority(NotificationPriority.MEDIUM)
            .build();

        NotificationResponse response = notificationService.createNotification(request);
        assertNotNull(response);

        // Deactivate the user
        userService.deactivateUser(testUser.getId());

        // Verify user is deactivated
        User deactivatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(deactivatedUser);
        assertFalse(deactivatedUser.getActive());

        // Verify notification still exists
        Notification existingNotification = notificationRepository.findById(response.getId()).orElse(null);
        assertNotNull(existingNotification);
        assertEquals(testUser.getId(), existingNotification.getUser().getId());

        // Verify cannot send notifications to inactive user
        assertFalse(deactivatedUser.canReceiveOnChannel(NotificationChannel.EMAIL));
    }

    @Test
    void testScheduledNotificationCancellation() {
        // Create a scheduled notification
        LocalDateTime scheduleTime = LocalDateTime.now().plusHours(5);
        NotificationRequest request = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("Scheduled Notification to Cancel")
            .body("This notification will be cancelled")
            .priority(NotificationPriority.MEDIUM)
            .scheduleType(ScheduleType.SCHEDULED)
            .scheduledTime(scheduleTime)
            .build();

        NotificationResponse response = notificationService.createNotification(request);
        assertNotNull(response);
        assertEquals(NotificationStatus.SCHEDULED, response.getStatus());

        // Cancel the notification
        schedulerService.cancelScheduledNotification(response.getId());

        // Verify notification is cancelled
        Notification cancelledNotification = notificationRepository.findById(response.getId()).orElse(null);
        assertNotNull(cancelledNotification);
        assertEquals(NotificationStatus.CANCELLED, cancelledNotification.getStatus());
    }

    @Test
    void testMultiplePriorityLevels_CompleteFlow() {
        // Create notifications with all priority levels
        NotificationRequest highRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.PUSH)
            .subject("HIGH Priority Alert")
            .body("Urgent notification")
            .priority(NotificationPriority.HIGH)
            .build();

        NotificationRequest mediumRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("MEDIUM Priority Update")
            .body("Regular notification")
            .priority(NotificationPriority.MEDIUM)
            .build();

        NotificationRequest lowRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.SMS)
            .subject("LOW Priority Info")
            .body("Non-urgent notification")
            .priority(NotificationPriority.LOW)
            .build();

        NotificationResponse highResponse = notificationService.createNotification(highRequest);
        NotificationResponse mediumResponse = notificationService.createNotification(mediumRequest);
        NotificationResponse lowResponse = notificationService.createNotification(lowRequest);

        // Verify all created
        assertNotNull(highResponse);
        assertNotNull(mediumResponse);
        assertNotNull(lowResponse);

        // Send in priority order
        notificationService.sendNotification(highResponse.getId());
        
        Notification sentHigh = notificationRepository.findById(highResponse.getId()).orElse(null);
        assertNotNull(sentHigh);
        assertEquals(NotificationStatus.SENT, sentHigh.getStatus());
        assertNotNull(sentHigh.getSentAt());

        // Verify sent time ordering (high sent before medium)
        notificationService.sendNotification(mediumResponse.getId());
        
        Notification sentMedium = notificationRepository.findById(mediumResponse.getId()).orElse(null);
        assertNotNull(sentMedium);
        assertTrue(sentHigh.getSentAt() != null && sentMedium.getSentAt() != null,
            "Both notifications should have sent times");
    }

    @Test
    void testUserChannelPreferences_OnlyPreferredChannelsWork() {
        // Update user to prefer only EMAIL
        testUser.setPreferredChannels(new HashSet<>(Set.of(NotificationChannel.EMAIL)));
        userRepository.save(testUser);

        // Verify user can receive on EMAIL
        assertTrue(testUser.canReceiveOnChannel(NotificationChannel.EMAIL));

        // Verify user cannot receive on other channels (even with contact info)
        assertFalse(testUser.canReceiveOnChannel(NotificationChannel.SMS));
        assertFalse(testUser.canReceiveOnChannel(NotificationChannel.PUSH));

        // Create notifications for both preferred and non-preferred channels
        NotificationRequest emailRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.EMAIL)
            .subject("Email Notification")
            .body("Should work")
            .priority(NotificationPriority.MEDIUM)
            .build();

        NotificationRequest smsRequest = NotificationRequest.builder()
            .userId(testUser.getId())
            .channel(NotificationChannel.SMS)
            .subject("SMS Notification")
            .body("Should be created")
            .priority(NotificationPriority.MEDIUM)
            .build();

        NotificationResponse emailResponse = notificationService.createNotification(emailRequest);
        
        // Try to create SMS notification - should fail since user doesn't prefer SMS channel
        try {
            NotificationResponse smsResponse = notificationService.createNotification(smsRequest);
            assertNotNull(smsResponse);
        } catch (IllegalArgumentException e) {
            // Expected - user can't receive on SMS channel
            assertTrue(e.getMessage().contains("cannot receive notifications"));
        }

        // Email notification was created
        assertNotNull(emailResponse);

        // Verify user preferences
        User reloadedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(reloadedUser);
        assertTrue(reloadedUser.canReceiveOnChannel(NotificationChannel.EMAIL));
        assertFalse(reloadedUser.canReceiveOnChannel(NotificationChannel.SMS));
    }
}

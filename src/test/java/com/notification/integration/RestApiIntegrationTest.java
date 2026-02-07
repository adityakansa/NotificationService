package com.notification.integration;

import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import com.notification.domain.enums.ScheduleType;
import com.notification.repository.NotificationRepository;
import com.notification.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API Integration Tests using JSON payloads
 * Tests complete flows with actual HTTP requests and JSON serialization
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        notificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateUserViaRestApi_WithJson() throws Exception {
        // JSON payload for creating a user
        String userJson = """
            {
                "username": "john.doe",
                "email": "john.doe@example.com",
                "phoneNumber": "+1234567890",
                "pushToken": "fcm-token-xyz",
                "preferredChannels": ["EMAIL", "SMS", "PUSH"]
            }
            """;

        // Create user via REST API
        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.username").value("john.doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.preferredChannels").isArray())
            .andExpect(jsonPath("$.preferredChannels", hasSize(3)))
            .andReturn();

        // Verify user was created in database
        long userCount = userRepository.count();
        assert userCount == 1;
    }

    @Test
    void testCreateNotificationViaRestApi_WithJson() throws Exception {
        // First, create a user
        String userJson = """
            {
                "username": "jane.smith",
                "email": "jane.smith@example.com",
                "phoneNumber": "+9876543210",
                "preferredChannels": ["EMAIL"]
            }
            """;

        MvcResult userResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn();

        String userResponse = userResult.getResponse().getContentAsString();
        Long userId = extractIdFromJson(userResponse);

        // Create notification via REST API
        String notificationJson = String.format("""
            {
                "userId": %d,
                "channel": "EMAIL",
                "subject": "Welcome Email",
                "body": "Welcome to our notification system!",
                "priority": "HIGH"
            }
            """, userId);

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.userId").value(userId))
            .andExpect(jsonPath("$.channel").value("EMAIL"))
            .andExpect(jsonPath("$.subject").value("Welcome Email"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.priority").value("HIGH"));

        // Verify notification was created
        long notificationCount = notificationRepository.count();
        assert notificationCount == 1;
    }

    @Test
    void testCompleteFlow_CreateUserAndScheduleNotification_WithJson() throws Exception {
        // Step 1: Create user
        String userJson = """
            {
                "username": "scheduler.user",
                "email": "scheduler@example.com",
                "phoneNumber": "+1112223333",
                "pushToken": "push-token-abc",
                "preferredChannels": ["EMAIL", "SMS", "PUSH"]
            }
            """;

        MvcResult userResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn();

        Long userId = extractIdFromJson(userResult.getResponse().getContentAsString());

        // Step 2: Schedule a notification
        String scheduledNotificationJson = String.format("""
            {
                "userId": %d,
                "channel": "EMAIL",
                "subject": "Scheduled Meeting Reminder",
                "body": "Your meeting is tomorrow at 10 AM",
                "priority": "MEDIUM",
                "scheduleType": "SCHEDULED",
                "scheduledTime": "2026-02-10T10:00:00"
            }
            """, userId);

        MvcResult notificationResult = mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(scheduledNotificationJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.status").value("SCHEDULED"))
            .andReturn();

        Long notificationId = extractIdFromJson(notificationResult.getResponse().getContentAsString());

        // Step 3: Reschedule the notification
        String rescheduleJson = """
            {
                "scheduledTime": "2026-02-11T14:00:00"
            }
            """;

        mockMvc.perform(put("/api/notifications/{id}/schedule", notificationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rescheduleJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(notificationId))
            .andExpect(jsonPath("$.status").value("SCHEDULED"));

        // Step 4: Get notification details
        mockMvc.perform(get("/api/notifications/{id}", notificationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(notificationId))
            .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void testBulkNotificationCreation_WithJson() throws Exception {
        // Create a user first
        String userJson = """
            {
                "username": "bulk.user",
                "email": "bulk@example.com",
                "phoneNumber": "+5556667777",
                "pushToken": "bulk-token",
                "preferredChannels": ["EMAIL", "SMS", "PUSH"]
            }
            """;

        MvcResult userResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andReturn();

        Long userId = extractIdFromJson(userResult.getResponse().getContentAsString());

        // Create bulk notifications
        String bulkNotificationJson = String.format("""
            {
                "userIds": [%d],
                "template": {
                    "userId": %d,
                    "channel": "EMAIL",
                    "subject": "System Announcement",
                    "body": "Important system update scheduled for maintenance",
                    "priority": "HIGH"
                }
            }
            """, userId, userId);

        mockMvc.perform(post("/api/notifications/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bulkNotificationJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    void testCreateMultipleUsersAndNotifications_WithJson() throws Exception {
        // Create multiple users
        String user1Json = """
            {
                "username": "alice",
                "email": "alice@example.com",
                "phoneNumber": "+1231231234",
                "preferredChannels": ["EMAIL"]
            }
            """;

        String user2Json = """
            {
                "username": "bob",
                "email": "bob@example.com",
                "phoneNumber": "+3213213210",
                "preferredChannels": ["SMS"]
            }
            """;

        MvcResult user1Result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(user1Json))
            .andExpect(status().isCreated())
            .andReturn();

        MvcResult user2Result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(user2Json))
            .andExpect(status().isCreated())
            .andReturn();

        Long user1Id = extractIdFromJson(user1Result.getResponse().getContentAsString());
        Long user2Id = extractIdFromJson(user2Result.getResponse().getContentAsString());

        // Create notifications for each user
        String notification1Json = String.format("""
            {
                "userId": %d,
                "channel": "EMAIL",
                "subject": "Welcome Alice",
                "body": "Hello Alice, welcome!",
                "priority": "MEDIUM"
            }
            """, user1Id);

        String notification2Json = String.format("""
            {
                "userId": %d,
                "channel": "SMS",
                "subject": "Welcome Bob",
                "body": "Hello Bob, welcome!",
                "priority": "LOW"
            }
            """, user2Id);

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notification1Json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.channel").value("EMAIL"));

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notification2Json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.channel").value("SMS"));

        // Verify counts
        assert userRepository.count() == 2;
        assert notificationRepository.count() == 2;
    }

    @Test
    void testHighPriorityNotificationFlow_WithJson() throws Exception {
        // Create user
        String userJson = """
            {
                "username": "urgent.user",
                "email": "urgent@example.com",
                "phoneNumber": "+9999999999",
                "pushToken": "urgent-push",
                "preferredChannels": ["EMAIL", "PUSH"]
            }
            """;

        MvcResult userResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andReturn();

        Long userId = extractIdFromJson(userResult.getResponse().getContentAsString());

        // Create HIGH priority notification
        String highPriorityJson = String.format("""
            {
                "userId": %d,
                "channel": "PUSH",
                "subject": "URGENT: Security Alert",
                "body": "Suspicious activity detected on your account",
                "priority": "HIGH"
            }
            """, userId);

        MvcResult notificationResult = mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(highPriorityJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.priority").value("HIGH"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

        Long notificationId = extractIdFromJson(notificationResult.getResponse().getContentAsString());

        // Send the notification
        mockMvc.perform(post("/api/notifications/{id}/send", notificationId))
            .andExpect(status().isOk());

        // Verify notification was sent
        mockMvc.perform(get("/api/notifications/{id}", notificationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(notificationId));
    }

    @Test
    void testUpdateUserChannelPreferences_WithJson() throws Exception {
        // Create user
        String userJson = """
            {
                "username": "prefs.user",
                "email": "prefs@example.com",
                "phoneNumber": "+4445556666",
                "preferredChannels": ["EMAIL"]
            }
            """;

        MvcResult userResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.preferredChannels", hasSize(1)))
            .andReturn();

        Long userId = extractIdFromJson(userResult.getResponse().getContentAsString());

        // Update channel preferences
        String updatePrefsJson = """
            ["EMAIL", "SMS", "PUSH"]
            """;

        mockMvc.perform(put("/api/users/{id}/preferences", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePrefsJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.preferredChannels", hasSize(3)))
            .andExpect(jsonPath("$.preferredChannels", hasItem("EMAIL")))
            .andExpect(jsonPath("$.preferredChannels", hasItem("SMS")))
            .andExpect(jsonPath("$.preferredChannels", hasItem("PUSH")));
    }

    @Test
    void testCancelScheduledNotification_WithJson() throws Exception {
        // Create user
        String userJson = """
            {
                "username": "cancel.user",
                "email": "cancel@example.com",
                "preferredChannels": ["EMAIL"]
            }
            """;

        MvcResult userResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andReturn();

        Long userId = extractIdFromJson(userResult.getResponse().getContentAsString());

        // Create scheduled notification
        String scheduledJson = String.format("""
            {
                "userId": %d,
                "channel": "EMAIL",
                "subject": "Future Event",
                "body": "Event scheduled for next week",
                "priority": "MEDIUM",
                "scheduleType": "SCHEDULED",
                "scheduledTime": "2026-02-15T10:00:00"
            }
            """, userId);

        MvcResult notificationResult = mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(scheduledJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("SCHEDULED"))
            .andReturn();

        Long notificationId = extractIdFromJson(notificationResult.getResponse().getContentAsString());

        // Cancel the notification
        mockMvc.perform(delete("/api/notifications/{id}/schedule", notificationId))
            .andExpect(status().isOk());

        // Verify notification is cancelled
        mockMvc.perform(get("/api/notifications/{id}", notificationId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void testGetAllUsersAndNotifications_WithJson() throws Exception {
        // Create 2 users
        String user1Json = """
            {
                "username": "list.user1",
                "email": "list1@example.com",
                "preferredChannels": ["EMAIL"]
            }
            """;

        String user2Json = """
            {
                "username": "list.user2",
                "email": "list2@example.com",
                "preferredChannels": ["SMS"]
            }
            """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(user1Json))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(user2Json))
            .andExpect(status().isCreated());

        // Get all users
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testInvalidUserCreation_WithJson() throws Exception {
        // Invalid JSON - blank email field (validation will catch this)
        String invalidUserJson = """
            {
                "username": "invalid.user",
                "phoneNumber": "+1234567890",
                "email": ""
            }
            """;

        // Expecting 400 because @NotBlank validation on email will fail
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateNotificationForNonExistentUser_WithJson() throws Exception {
        // Try to create notification for non-existent user
        String notificationJson = """
            {
                "userId": 99999,
                "channel": "EMAIL",
                "subject": "Test",
                "body": "This should fail",
                "priority": "MEDIUM"
            }
            """;

        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
            .andExpect(status().isBadRequest());
    }

    /**
     * Helper method to extract ID from JSON response
     */
    private Long extractIdFromJson(String json) {
        try {
            int idStart = json.indexOf("\"id\":") + 5;
            int idEnd = json.indexOf(",", idStart);
            if (idEnd == -1) {
                idEnd = json.indexOf("}", idStart);
            }
            return Long.parseLong(json.substring(idStart, idEnd).trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract ID from JSON: " + json, e);
        }
    }
}

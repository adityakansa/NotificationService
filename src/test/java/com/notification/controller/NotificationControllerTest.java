package com.notification.controller;

import com.notification.domain.entity.Notification;
import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.domain.enums.NotificationChannel;
import com.notification.domain.enums.NotificationPriority;
import com.notification.domain.enums.NotificationStatus;
import com.notification.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NotificationController
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationSchedulerService schedulerService;

    @Mock
    private NotificationBatchService batchService;

    @Mock
    private NotificationRetryService retryService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateNotification_Success() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setSubject("Test");
        request.setBody("Test body");
        request.setChannel(NotificationChannel.EMAIL);

        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .userId(1L)
            .subject("Test")
            .body("Test body")
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PENDING)
            .build();

        when(notificationService.createNotification(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.subject").value("Test"));

        verify(notificationService).createNotification(any());
    }

    @Test
    void testGetNotification_Success() throws Exception {
        // Arrange
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .userId(1L)
            .subject("Test")
            .status(NotificationStatus.SENT)
            .build();

        when(notificationService.getNotification(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(notificationService).getNotification(1L);
    }

    @Test
    void testGetAllNotifications_Success() throws Exception {
        // Arrange
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .userId(1L)
            .subject("Test")
            .build();

        when(notificationService.getAllNotifications()).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(notificationService).getAllNotifications();
    }

    @Test
    void testGetUserNotifications_Success() throws Exception {
        // Arrange
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .userId(1L)
            .build();

        when(notificationService.getUserNotifications(1L)).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(notificationService).getUserNotifications(1L);
    }

    @Test
    void testSendNotification_Success() throws Exception {
        // Arrange
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .status(NotificationStatus.SENT)
            .build();

        when(notificationService.sendNotification(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/1/send"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(notificationService).sendNotification(1L);
    }

    @Test
    void testCancelScheduledNotification_Success() throws Exception {
        // Arrange
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .status(NotificationStatus.CANCELLED)
            .build();

        doNothing().when(schedulerService).cancelScheduledNotification(1L);
        when(notificationService.getNotification(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/notifications/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(schedulerService).cancelScheduledNotification(1L);
    }

    @Test
    void testRescheduleNotification_Success() throws Exception {
        // Arrange
        Map<String, String> payload = Map.of("scheduledTime", "2026-02-10T10:00:00");
        Notification rescheduled = Notification.builder().id(1L).status(NotificationStatus.SCHEDULED).build();
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .status(NotificationStatus.SCHEDULED)
            .build();

        when(schedulerService.rescheduleNotification(eq(1L), any())).thenReturn(rescheduled);
        when(notificationService.getNotification(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/notifications/1/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(schedulerService).rescheduleNotification(eq(1L), any());
    }

    @Test
    void testRetryNotification_Success() throws Exception {
        // Arrange
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .status(NotificationStatus.RETRY)
            .build();

        doNothing().when(retryService).manualRetry(1L);
        when(notificationService.getNotification(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/1/retry"))
                .andExpect(status().isOk());

        verify(retryService).manualRetry(1L);
    }

    @Test
    void testResetNotification_Success() throws Exception {
        // Arrange
        NotificationResponse response = NotificationResponse.builder()
            .id(1L)
            .status(NotificationStatus.PENDING)
            .build();

        doNothing().when(retryService).resetForRetry(1L);
        when(notificationService.getNotification(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/1/reset"))
                .andExpect(status().isOk());

        verify(retryService).resetForRetry(1L);
    }

    @Test
    void testProcessPendingNotifications_Success() throws Exception {
        // Arrange
        when(notificationService.processPendingNotifications()).thenReturn(5);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedCount").value(5));

        verify(notificationService).processPendingNotifications();
    }

    @Test
    void testGetBatchStatistics_Success() throws Exception {
        // Arrange
        Map<String, Object> stats = Map.of("batchSize", 100, "pendingCount", 5L);
        when(batchService.getBatchStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/notifications/statistics/batch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchSize").value(100))
                .andExpect(jsonPath("$.pendingCount").value(5));

        verify(batchService).getBatchStatistics();
    }

    @Test
    void testGetRetryStatistics_Success() throws Exception {
        // Arrange
        Map<String, Object> stats = Map.of("retryCount", 3L);
        when(retryService.getRetryStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/notifications/statistics/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retryCount").value(3));

        verify(retryService).getRetryStatistics();
    }
}

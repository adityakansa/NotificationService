package com.notification.controller;

import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.exception.GlobalExceptionHandler;
import com.notification.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
        
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .active(true)
            .preferredChannels(new HashSet<>(Set.of(NotificationChannel.EMAIL)))
            .build();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Arrange
        when(userService.registerUser(any())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).registerUser(any());
    }

    @Test
    void testGetUser_Success() throws Exception {
        // Arrange
        when(userService.getUser(1L)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUser(1L);
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(userService).getAllUsers();
    }

    @Test
    void testUpdatePreferences_Success() throws Exception {
        // Arrange
        Set<NotificationChannel> channels = Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS);
        testUser.setPreferredChannels(channels);
        
        when(userService.updateChannelPreferences(eq(1L), any())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of("EMAIL", "SMS"))))
                .andExpect(status().isOk());

        verify(userService).updateChannelPreferences(eq(1L), any());
    }

    @Test
    void testUpdateChannelContact_Success() throws Exception {
        // Arrange
        Map<String, String> payload = Map.of("contact", "new@example.com");
        testUser.setEmail("new@example.com");
        
        when(userService.updateChannelContact(1L, NotificationChannel.EMAIL, "new@example.com"))
            .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1/channels/EMAIL")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));

        verify(userService).updateChannelContact(1L, NotificationChannel.EMAIL, "new@example.com");
    }

    @Test
    void testEnableChannel_Success() throws Exception {
        // Arrange
        testUser.getPreferredChannels().add(NotificationChannel.SMS);
        when(userService.enableChannel(1L, NotificationChannel.SMS)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(patch("/api/users/1/channels/SMS"))
                .andExpect(status().isOk());

        verify(userService).enableChannel(1L, NotificationChannel.SMS);
    }

    @Test
    void testDisableChannel_Success() throws Exception {
        // Arrange
        when(userService.disableChannel(1L, NotificationChannel.EMAIL)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1/channels/EMAIL"))
                .andExpect(status().isOk());

        verify(userService).disableChannel(1L, NotificationChannel.EMAIL);
    }

    @Test
    void testDeactivateUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deactivateUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deactivateUser(1L);
    }

    @Test
    void testGetUser_NotFound() throws Exception {
        // Arrange
        when(userService.getUser(999L))
            .thenThrow(new IllegalArgumentException("User not found: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().is4xxClientError());

        verify(userService).getUser(999L);
    }
}

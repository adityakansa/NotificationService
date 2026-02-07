package com.notification.service;

import com.notification.domain.entity.User;
import com.notification.domain.enums.NotificationChannel;
import com.notification.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("+1234567890")
            .active(true)
            .preferredChannels(new HashSet<>(Set.of(NotificationChannel.EMAIL)))
            .build();
    }

    // ==================== Register User Tests ====================

    @Test
    void testRegisterUser_Success() {
        // Arrange
        User newUser = User.builder()
            .username("newuser")
            .email("newuser@example.com")
            .build();
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.registerUser(newUser);

        // Assert
        assertNotNull(result);
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository).save(newUser);
    }

    @Test
    void testRegisterUser_DuplicateUsername_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        User newUser = User.builder()
            .username("testuser")
            .email("new@example.com")
            .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.registerUser(newUser)
        );
        
        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        User newUser = User.builder()
            .username("newuser")
            .email("test@example.com")
            .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.registerUser(newUser)
        );
        
        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository, never()).save(any());
    }

    // ==================== Get User Tests ====================

    @Test
    void testGetUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUser_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.getUser(999L)
        );
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(999L);
    }

    // ==================== Get User By Username Tests ====================

    @Test
    void testGetUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsername_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.getUserByUsername("unknown")
        );
        
        assertTrue(exception.getMessage().contains("User not found"));
    }

    // ==================== Update Channel Preferences Tests ====================

    @Test
    void testUpdateChannelPreferences_Success() {
        // Arrange
        Set<NotificationChannel> newChannels = Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateChannelPreferences(1L, newChannels);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateChannelPreferences_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.updateChannelPreferences(999L, Set.of(NotificationChannel.EMAIL))
        );
    }

    // ==================== Get All Users Tests ====================

    @Test
    void testGetAllUsers_Success() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Deactivate User Tests ====================

    @Test
    void testDeactivateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deactivateUser(1L);

        // Assert
        assertFalse(testUser.getActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeactivateUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.deactivateUser(999L)
        );
    }

    // ==================== Update Channel Contact Tests ====================

    @Test
    void testUpdateChannelContact_Email_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateChannelContact(1L, NotificationChannel.EMAIL, "newemail@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("newemail@example.com", testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateChannelContact_SMS_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateChannelContact(1L, NotificationChannel.SMS, "+9876543210");

        // Assert
        assertNotNull(result);
        assertEquals("+9876543210", testUser.getPhoneNumber());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateChannelContact_Push_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateChannelContact(1L, NotificationChannel.PUSH, "new-token-123");

        // Assert
        assertNotNull(result);
        assertEquals("new-token-123", testUser.getPushToken());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateChannelContact_WhatsApp_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateChannelContact(1L, NotificationChannel.WHATSAPP, "+1122334455");

        // Assert
        assertNotNull(result);
        assertEquals("+1122334455", testUser.getWhatsappNumber());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateChannelContact_Slack_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateChannelContact(1L, NotificationChannel.SLACK, "@slackuser");

        // Assert
        assertNotNull(result);
        assertEquals("@slackuser", testUser.getSlackHandle());
        verify(userRepository).save(testUser);
    }

    // ==================== Enable Channel Tests ====================

    @Test
    void testEnableChannel_Success() {
        // Arrange
        testUser.setPhoneNumber("+1234567890");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.enableChannel(1L, NotificationChannel.SMS);

        // Assert
        assertNotNull(result);
        assertTrue(testUser.getPreferredChannels().contains(NotificationChannel.SMS));
        verify(userRepository).save(testUser);
    }

    @Test
    void testEnableChannel_NoContactInfo_ThrowsException() {
        // Arrange
        testUser.setPhoneNumber(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> userService.enableChannel(1L, NotificationChannel.SMS)
        );
        
        assertTrue(exception.getMessage().contains("Contact information not set"));
        verify(userRepository, never()).save(any());
    }

    // ==================== Disable Channel Tests ====================

    @Test
    void testDisableChannel_Success() {
        // Arrange
        testUser.getPreferredChannels().add(NotificationChannel.EMAIL);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.disableChannel(1L, NotificationChannel.EMAIL);

        // Assert
        assertNotNull(result);
        assertFalse(testUser.getPreferredChannels().contains(NotificationChannel.EMAIL));
        verify(userRepository).save(testUser);
    }

    @Test
    void testDisableChannel_NotEnabled_StillSucceeds() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.disableChannel(1L, NotificationChannel.PUSH);

        // Assert
        assertNotNull(result);
        assertFalse(testUser.getPreferredChannels().contains(NotificationChannel.PUSH));
        verify(userRepository).save(testUser);
    }
}

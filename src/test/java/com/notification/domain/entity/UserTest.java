package com.notification.domain.entity;

import com.notification.domain.enums.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 */
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("+1234567890")
            .pushToken("push-token-123")
            .whatsappNumber("+9876543210")
            .slackHandle("@testuser")
            .active(true)
            .preferredChannels(new HashSet<>(Set.of(NotificationChannel.EMAIL, NotificationChannel.SMS)))
            .personalizationVariables(new java.util.HashMap<>())
            .build();
    }

    @Test
    void testUserBuilder() {
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getActive());
    }

    @Test
    void testGetContactForChannel_Email() {
        String contact = user.getContactForChannel(NotificationChannel.EMAIL);
        assertEquals("test@example.com", contact);
    }

    @Test
    void testGetContactForChannel_SMS() {
        String contact = user.getContactForChannel(NotificationChannel.SMS);
        assertEquals("+1234567890", contact);
    }

    @Test
    void testGetContactForChannel_Push() {
        String contact = user.getContactForChannel(NotificationChannel.PUSH);
        assertEquals("push-token-123", contact);
    }

    @Test
    void testGetContactForChannel_WhatsApp() {
        String contact = user.getContactForChannel(NotificationChannel.WHATSAPP);
        assertEquals("+9876543210", contact);
    }

    @Test
    void testGetContactForChannel_Slack() {
        String contact = user.getContactForChannel(NotificationChannel.SLACK);
        assertEquals("@testuser", contact);
    }

    @Test
    void testCanReceiveOnChannel_Success() {
        boolean canReceive = user.canReceiveOnChannel(NotificationChannel.EMAIL);
        assertTrue(canReceive);
    }

    @Test
    void testCanReceiveOnChannel_NotInPreferredChannels() {
        boolean canReceive = user.canReceiveOnChannel(NotificationChannel.PUSH);
        assertFalse(canReceive);
    }

    @Test
    void testCanReceiveOnChannel_NoContactInfo() {
        user.setPushToken(null);
        user.getPreferredChannels().add(NotificationChannel.PUSH);
        
        boolean canReceive = user.canReceiveOnChannel(NotificationChannel.PUSH);
        assertFalse(canReceive);
    }

    @Test
    void testCanReceiveOnChannel_UserNotActive() {
        user.setActive(false);
        
        boolean canReceive = user.canReceiveOnChannel(NotificationChannel.EMAIL);
        assertFalse(canReceive);
    }

    @Test
    void testPrePersist() {
        User newUser = new User();
        newUser.onCreate();
        
        assertNotNull(newUser.getCreatedAt());
        assertNotNull(newUser.getUpdatedAt());
        assertTrue(newUser.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testPreUpdate() {
        LocalDateTime originalTime = LocalDateTime.now().minusMinutes(5);
        user.setUpdatedAt(originalTime);
        
        user.onUpdate();
        
        assertTrue(user.getUpdatedAt().isAfter(originalTime));
    }

    @Test
    void testPreferredChannelsManagement() {
        user.getPreferredChannels().add(NotificationChannel.PUSH);
        assertTrue(user.getPreferredChannels().contains(NotificationChannel.PUSH));
        
        user.getPreferredChannels().remove(NotificationChannel.SMS);
        assertFalse(user.getPreferredChannels().contains(NotificationChannel.SMS));
    }

    @Test
    void testPersonalizationVariables() {
        user.getPersonalizationVariables().put("firstName", "John");
        user.getPersonalizationVariables().put("lastName", "Doe");
        
        assertEquals("John", user.getPersonalizationVariables().get("firstName"));
        assertEquals(2, user.getPersonalizationVariables().size());
    }

    @Test
    void testAllArgsConstructor() {
        User fullUser = new User(
            1L, "user", "user@test.com", "+123", "token", "+456", "@slack",
            true, new HashSet<>(), new java.util.HashMap<>(),
            LocalDateTime.now(), LocalDateTime.now()
        );
        
        assertNotNull(fullUser);
        assertEquals("user", fullUser.getUsername());
    }

    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
        assertNull(emptyUser.getId());
    }

    @Test
    void testSettersAndGetters() {
        user.setUsername("newusername");
        assertEquals("newusername", user.getUsername());
        
        user.setEmail("newemail@test.com");
        assertEquals("newemail@test.com", user.getEmail());
        
        user.setActive(false);
        assertFalse(user.getActive());
    }
}

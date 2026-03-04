package id.ac.ui.cs.advprog.auth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserModelTest {

    @Test
    void userGettersSettersWork() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setUsername("user1");
        user.setEmail("user1@mail.com");
        user.setName("User One");
        user.setPasswordHash("hashed-secret");
        user.setRole(UserRole.SUPIR_TRUK);
        user.setMandorCertificationNumber(null);
        user.setOauthProvider(null);
        user.setOauthProviderId(null);
        user.setActive(true);

        assertEquals(id, user.getId());
        assertEquals("user1", user.getUsername());
        assertEquals("user1@mail.com", user.getEmail());
        assertEquals("User One", user.getName());
        assertEquals("hashed-secret", user.getPasswordHash());
        assertEquals(UserRole.SUPIR_TRUK, user.getRole());
        assertNull(user.getMandorCertificationNumber());
        assertNull(user.getOauthProvider());
        assertNull(user.getOauthProviderId());
        assertTrue(user.isActive());
    }

    @Test
    void userBuilderWorks() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .username("mandor1")
                .email("mandor@mail.com")
                .name("Mandor One")
                .passwordHash("bcrypt-hash")
                .role(UserRole.MANDOR)
                .mandorCertificationNumber("CERT-001")
                .isActive(true)
                .build();

        assertEquals(id, user.getId());
        assertEquals("mandor1", user.getUsername());
        assertEquals("mandor@mail.com", user.getEmail());
        assertEquals("Mandor One", user.getName());
        assertEquals("bcrypt-hash", user.getPasswordHash());
        assertEquals(UserRole.MANDOR, user.getRole());
        assertEquals("CERT-001", user.getMandorCertificationNumber());
        assertTrue(user.isActive());
    }

    @Test
    void isActiveDefaultsToTrue() {
        User user = User.builder()
                .username("u")
                .email("u@mail.com")
                .name("U")
                .role(UserRole.BURUH)
                .build();
        assertTrue(user.isActive());
    }

    @Test
    void prePersistSetsTimestamps() {
        User user = new User();
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void preUpdateSetsUpdatedAt() {
        User user = new User();
        user.onCreate();
        var initialUpdatedAt = user.getUpdatedAt();

        user.onUpdate();

        assertNotNull(user.getUpdatedAt());
        // updatedAt should be >= initialUpdatedAt
        assertFalse(user.getUpdatedAt().isBefore(initialUpdatedAt));
    }
}

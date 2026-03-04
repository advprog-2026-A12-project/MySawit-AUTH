package id.ac.ui.cs.advprog.auth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenModelTest {

    private User sampleUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("test-user")
                .email("test@mail.com")
                .name("Test")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();
    }

    @Test
    void gettersAndSettersWork() {
        RefreshToken rt = new RefreshToken();
        UUID id = UUID.randomUUID();
        User user = sampleUser();
        Instant expiry = Instant.now().plus(7, ChronoUnit.DAYS);

        rt.setId(id);
        rt.setUser(user);
        rt.setTokenHash("abc123");
        rt.setDeviceInfo("Chrome/Windows");
        rt.setRevoked(false);
        rt.setExpiresAt(expiry);

        assertEquals(id, rt.getId());
        assertEquals(user, rt.getUser());
        assertEquals("abc123", rt.getTokenHash());
        assertEquals("Chrome/Windows", rt.getDeviceInfo());
        assertFalse(rt.isRevoked());
        assertEquals(expiry, rt.getExpiresAt());
    }

    @Test
    void builderWorks() {
        User user = sampleUser();
        Instant expiry = Instant.now().plus(7, ChronoUnit.DAYS);

        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("hash123")
                .expiresAt(expiry)
                .build();

        assertNotNull(rt.getId());
        assertEquals(user, rt.getUser());
        assertEquals("hash123", rt.getTokenHash());
        assertFalse(rt.isRevoked()); // default
        assertNull(rt.getDeviceInfo());
    }

    @Test
    void prePersistSetsCreatedAt() {
        RefreshToken rt = new RefreshToken();
        assertNull(rt.getCreatedAt());

        rt.onCreate();

        assertNotNull(rt.getCreatedAt());
    }

    @Test
    void isExpiredReturnsTrueForPastExpiry() {
        RefreshToken rt = RefreshToken.builder()
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        assertTrue(rt.isExpired());
    }

    @Test
    void isExpiredReturnsFalseForFutureExpiry() {
        RefreshToken rt = RefreshToken.builder()
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        assertFalse(rt.isExpired());
    }

    @Test
    void isUsableReturnsTrueWhenNotRevokedAndNotExpired() {
        RefreshToken rt = RefreshToken.builder()
                .isRevoked(false)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        assertTrue(rt.isUsable());
    }

    @Test
    void isUsableReturnsFalseWhenRevoked() {
        RefreshToken rt = RefreshToken.builder()
                .isRevoked(true)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        assertFalse(rt.isUsable());
    }

    @Test
    void isUsableReturnsFalseWhenExpired() {
        RefreshToken rt = RefreshToken.builder()
                .isRevoked(false)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        assertFalse(rt.isUsable());
    }
}

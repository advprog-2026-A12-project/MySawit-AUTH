package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.model.User;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "bXlTYXdpdEF1dGhTZXJ2aWNlSldUU2VjcmV0S2V5Rm9yVGVzdGluZ011c3RCZUxvbmdFbm91Z2hGb3JIUzI1Ng==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 900, 604800);
    }

    private User sampleUser() {
        return User.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();
    }

    @Test
    void generateAccessTokenReturnsNonNull() {
        String token = jwtService.generateAccessToken(sampleUser());
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractAllClaimsFromValidToken() {
        User user = sampleUser();
        String token = jwtService.generateAccessToken(user);

        Claims claims = jwtService.extractAllClaims(token);

        assertEquals(user.getId().toString(), claims.getSubject());
        assertNull(claims.get("email", String.class));
        assertNull(claims.get("name", String.class));
        assertEquals("BURUH", claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractUserIdWorksCorrectly() {
        User user = sampleUser();
        String token = jwtService.generateAccessToken(user);

        UUID userId = jwtService.extractUserId(token);

        assertEquals(user.getId(), userId);
    }

    @Test
    void extractRoleWorksCorrectly() {
        User user = sampleUser();
        String token = jwtService.generateAccessToken(user);

        String role = jwtService.extractRole(token);

        assertEquals("BURUH", role);
    }

    @Test
    void isTokenValidReturnsTrueForValidToken() {
        String token = jwtService.generateAccessToken(sampleUser());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValidReturnsFalseForTamperedToken() {
        String token = jwtService.generateAccessToken(sampleUser());
        String tampered = token + "x";
        assertFalse(jwtService.isTokenValid(tampered));
    }

    @Test
    void isTokenValidReturnsFalseForGarbage() {
        assertFalse(jwtService.isTokenValid("not.a.jwt"));
    }

    @Test
    void isTokenValidReturnsFalseForNull() {
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    void isTokenValidReturnsFalseForExpiredToken() {
        // Create a service with 0-second expiration
        JwtService shortLived = new JwtService(TEST_SECRET, 0, 0);
        String token = shortLived.generateAccessToken(sampleUser());

        // Token is already expired
        assertFalse(shortLived.isTokenValid(token));
    }

    @Test
    void generateRefreshTokenReturnsNonBlankString() {
        String refreshToken = jwtService.generateRefreshToken();
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isBlank());
    }

    @Test
    void generateRefreshTokenIsUnique() {
        String token1 = jwtService.generateRefreshToken();
        String token2 = jwtService.generateRefreshToken();
        assertNotEquals(token1, token2);
    }

    @Test
    void hashTokenReturnsDeterministicHash() {
        String token = "test-token-value";
        String hash1 = jwtService.hashToken(token);
        String hash2 = jwtService.hashToken(token);

        assertNotNull(hash1);
        assertEquals(64, hash1.length()); // SHA-256 = 64 hex chars
        assertEquals(hash1, hash2);
    }

    @Test
    void hashTokenReturnsDifferentHashForDifferentInput() {
        String hash1 = jwtService.hashToken("token-a");
        String hash2 = jwtService.hashToken("token-b");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void getAccessTokenExpirationReturnsConfiguredValue() {
        assertEquals(900, jwtService.getAccessTokenExpiration());
    }

    @Test
    void getRefreshTokenExpirationReturnsConfiguredValue() {
        assertEquals(604800, jwtService.getRefreshTokenExpiration());
    }

    @Test
    void extractAllClaimsThrowsForInvalidToken() {
        assertThrows(Exception.class, () -> jwtService.extractAllClaims("invalid"));
    }

    @Test
    void isTokenValidReturnsFalseForWrongSecret() {
        // Generate with a different key
        String otherSecret =
                "YW5vdGhlclNlY3JldEtleVRoYXRJc0RpZmZlcmVudEZyb21UaGVPcmlnaW5hbFNlY3JldEtleUhlcmU=";
        JwtService otherService = new JwtService(otherSecret, 900, 604800);
        String token = otherService.generateAccessToken(sampleUser());

        assertFalse(jwtService.isTokenValid(token));
    }
}

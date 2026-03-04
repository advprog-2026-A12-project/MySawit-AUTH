package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RefreshTokenRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.response.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.TokenRefreshResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.model.RefreshToken;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .passwordHash("$2a$12$hashedPassword")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();
        sampleUser.setCreatedAt(java.time.Instant.now());
        sampleUser.setUpdatedAt(java.time.Instant.now());
    }

    // ── Register ────────────────────────────────────────────────────────

    @Nested
    class RegisterTests {

        @Test
        void registerSuccessForBuruh() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Ahmad Buruh")
                    .email("ahmad@example.com")
                    .password("SecureP@ss123")
                    .role("BURUH")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                u.setCreatedAt(java.time.Instant.now());
                u.setUpdatedAt(java.time.Instant.now());
                return u;
            });

            RegisterResponseData result = authService.register(request);

            assertNotNull(result.getId());
            assertEquals("ahmad@example.com", result.getEmail());
            assertEquals("Ahmad Buruh", result.getName());
            assertEquals("BURUH", result.getRole());
            assertNotNull(result.getCreatedAt());
            verify(userRepository).save(any(User.class));
        }

        @Test
        void registerSuccessForMandorWithCertification() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Budi Mandor")
                    .email("budi@example.com")
                    .password("SecureP@ss123")
                    .role("MANDOR")
                    .mandorCertificationNumber("CERT-001")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByMandorCertificationNumber(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                u.setCreatedAt(java.time.Instant.now());
                u.setUpdatedAt(java.time.Instant.now());
                return u;
            });

            RegisterResponseData result = authService.register(request);

            assertEquals("MANDOR", result.getRole());
            verify(userRepository).existsByMandorCertificationNumber("CERT-001");
        }

        @Test
        void registerThrowsForAdminRole() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Admin")
                    .email("admin@example.com")
                    .password("SecureP@ss123")
                    .role("ADMIN")
                    .build();

            assertThrows(UnprocessableEntityException.class,
                    () -> authService.register(request));
        }

        @Test
        void registerThrowsForInvalidRole() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test")
                    .email("test@example.com")
                    .password("SecureP@ss123")
                    .role("INVALID_ROLE")
                    .build();

            assertThrows(InvalidUserRequestException.class,
                    () -> authService.register(request));
        }

        @Test
        void registerThrowsForMandorWithoutCertification() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Mandor")
                    .email("mandor@example.com")
                    .password("SecureP@ss123")
                    .role("MANDOR")
                    .mandorCertificationNumber(null)
                    .build();

            assertThrows(UnprocessableEntityException.class,
                    () -> authService.register(request));
        }

        @Test
        void registerThrowsForMandorWithBlankCertification() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Mandor")
                    .email("mandor@example.com")
                    .password("SecureP@ss123")
                    .role("MANDOR")
                    .mandorCertificationNumber("   ")
                    .build();

            assertThrows(UnprocessableEntityException.class,
                    () -> authService.register(request));
        }

        @Test
        void registerThrowsForDuplicateEmail() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Test")
                    .email("existing@example.com")
                    .password("SecureP@ss123")
                    .role("BURUH")
                    .build();

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThrows(DuplicateUserException.class,
                    () -> authService.register(request));
        }

        @Test
        void registerThrowsForDuplicateMandorCertification() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Mandor")
                    .email("new-mandor@example.com")
                    .password("SecureP@ss123")
                    .role("MANDOR")
                    .mandorCertificationNumber("CERT-EXISTING")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByMandorCertificationNumber("CERT-EXISTING")).thenReturn(true);

            assertThrows(DuplicateUserException.class,
                    () -> authService.register(request));
        }

        @Test
        void registerGeneratesUsernameFromName() {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Ahmad Buruh")
                    .email("ahmad@example.com")
                    .password("SecureP@ss123")
                    .role("BURUH")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                u.setCreatedAt(java.time.Instant.now());
                u.setUpdatedAt(java.time.Instant.now());
                return u;
            });

            authService.register(request);

            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser.getUsername());
            // Username should start with "ahmad-buruh-"
            assertEquals("ahmad-buruh", savedUser.getUsername().substring(0,
                    savedUser.getUsername().lastIndexOf('-')));
        }
    }

    // ── Login ───────────────────────────────────────────────────────────

    @Nested
    class LoginTests {

        @Test
        void loginSuccessReturnsTokens() {
            LoginRequest request = LoginRequest.builder()
                    .email("ahmad@example.com")
                    .password("SecureP@ss123")
                    .build();

            when(userRepository.findByEmail("ahmad@example.com"))
                    .thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("SecureP@ss123", sampleUser.getPasswordHash()))
                    .thenReturn(true);
            when(jwtService.generateAccessToken(sampleUser)).thenReturn("access-jwt");
            when(jwtService.generateRefreshToken()).thenReturn("raw-refresh");
            when(jwtService.hashToken("raw-refresh")).thenReturn("hashed-refresh");
            when(jwtService.getAccessTokenExpiration()).thenReturn(900L);
            when(jwtService.getRefreshTokenExpiration()).thenReturn(604800L);

            LoginResponseData result = authService.login(request);

            assertEquals("access-jwt", result.getAccessToken());
            assertEquals("raw-refresh", result.getRefreshToken());
            assertEquals("Bearer", result.getTokenType());
            assertEquals(900, result.getExpiresIn());
            assertNotNull(result.getUser());
            assertEquals(sampleUser.getId(), result.getUser().getId());
            assertEquals("BURUH", result.getUser().getRole());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        void loginThrowsWhenEmailNotFound() {
            LoginRequest request = LoginRequest.builder()
                    .email("nonexistent@example.com")
                    .password("pass")
                    .build();

            when(userRepository.findByEmail("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertEquals("Invalid email or password", ex.getMessage());
        }

        @Test
        void loginThrowsWhenAccountDeactivated() {
            sampleUser.setActive(false);
            LoginRequest request = LoginRequest.builder()
                    .email("ahmad@example.com")
                    .password("SecureP@ss123")
                    .build();

            when(userRepository.findByEmail("ahmad@example.com"))
                    .thenReturn(Optional.of(sampleUser));

            UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
            assertEquals("Account is deactivated", ex.getMessage());
        }

        @Test
        void loginThrowsWhenPasswordWrong() {
            LoginRequest request = LoginRequest.builder()
                    .email("ahmad@example.com")
                    .password("WrongPass123!")
                    .build();

            when(userRepository.findByEmail("ahmad@example.com"))
                    .thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("WrongPass123!", sampleUser.getPasswordHash()))
                    .thenReturn(false);

            assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
        }

        @Test
        void loginThrowsWhenPasswordHashIsNull() {
            sampleUser.setPasswordHash(null);
            LoginRequest request = LoginRequest.builder()
                    .email("ahmad@example.com")
                    .password("SecureP@ss123")
                    .build();

            when(userRepository.findByEmail("ahmad@example.com"))
                    .thenReturn(Optional.of(sampleUser));

            assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));
        }
    }

    // ── Logout ──────────────────────────────────────────────────────────

    @Nested
    class LogoutTests {

        @Test
        void logoutRevokesExistingToken() {
            LogoutRequest request = LogoutRequest.builder()
                    .refreshToken("raw-token")
                    .build();

            RefreshToken storedToken = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .user(sampleUser)
                    .tokenHash("hashed")
                    .isRevoked(false)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            when(jwtService.hashToken("raw-token")).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed"))
                    .thenReturn(Optional.of(storedToken));

            authService.logout(request);

            verify(refreshTokenRepository).save(storedToken);
        }

        @Test
        void logoutDoesNothingWhenTokenNotFound() {
            LogoutRequest request = LogoutRequest.builder()
                    .refreshToken("unknown-token")
                    .build();

            when(jwtService.hashToken("unknown-token")).thenReturn("hashed-unknown");
            when(refreshTokenRepository.findByTokenHash("hashed-unknown"))
                    .thenReturn(Optional.empty());

            authService.logout(request);

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        void logoutDoesNothingWhenTokenAlreadyRevoked() {
            LogoutRequest request = LogoutRequest.builder()
                    .refreshToken("revoked-token")
                    .build();

            RefreshToken storedToken = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .user(sampleUser)
                    .tokenHash("hashed-revoked")
                    .isRevoked(true)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            when(jwtService.hashToken("revoked-token")).thenReturn("hashed-revoked");
            when(refreshTokenRepository.findByTokenHash("hashed-revoked"))
                    .thenReturn(Optional.of(storedToken));

            authService.logout(request);

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    // ── Refresh ─────────────────────────────────────────────────────────

    @Nested
    class RefreshTests {

        private RefreshToken validStoredToken;

        @BeforeEach
        void setUpRefresh() {
            validStoredToken = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .user(sampleUser)
                    .tokenHash("old-hash")
                    .isRevoked(false)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();
        }

        @Test
        void refreshReturnsNewTokens() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("old-raw-token")
                    .build();

            when(jwtService.hashToken("old-raw-token")).thenReturn("old-hash");
            when(refreshTokenRepository.findByTokenHash("old-hash"))
                    .thenReturn(Optional.of(validStoredToken));
            when(jwtService.generateAccessToken(sampleUser)).thenReturn("new-access");
            when(jwtService.generateRefreshToken()).thenReturn("new-raw-refresh");
            when(jwtService.hashToken("new-raw-refresh")).thenReturn("new-hash");
            when(jwtService.getAccessTokenExpiration()).thenReturn(900L);
            when(jwtService.getRefreshTokenExpiration()).thenReturn(604800L);

            TokenRefreshResponseData result = authService.refresh(request);

            assertEquals("new-access", result.getAccessToken());
            assertEquals("new-raw-refresh", result.getRefreshToken());
            assertEquals("Bearer", result.getTokenType());
            assertEquals(900, result.getExpiresIn());

            // Old token should be revoked (save #1), new token should be persisted (save #2)
            verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshToken.class));
        }

        @Test
        void refreshThrowsWhenTokenNotFound() {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("unknown")
                    .build();

            when(jwtService.hashToken("unknown")).thenReturn("unknown-hash");
            when(refreshTokenRepository.findByTokenHash("unknown-hash"))
                    .thenReturn(Optional.empty());

            assertThrows(InvalidTokenException.class,
                    () -> authService.refresh(request));
        }

        @Test
        void refreshThrowsAndRevokesAllWhenTokenAlreadyRevoked() {
            validStoredToken.setRevoked(true);
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("reused-token")
                    .build();

            when(jwtService.hashToken("reused-token")).thenReturn("old-hash");
            when(refreshTokenRepository.findByTokenHash("old-hash"))
                    .thenReturn(Optional.of(validStoredToken));

            assertThrows(InvalidTokenException.class,
                    () -> authService.refresh(request));

            // All tokens for the user should be revoked (token reuse detection)
            verify(refreshTokenRepository).revokeAllByUser(sampleUser);
        }

        @Test
        void refreshThrowsWhenTokenExpired() {
            validStoredToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("expired-token")
                    .build();

            when(jwtService.hashToken("expired-token")).thenReturn("old-hash");
            when(refreshTokenRepository.findByTokenHash("old-hash"))
                    .thenReturn(Optional.of(validStoredToken));

            assertThrows(InvalidTokenException.class,
                    () -> authService.refresh(request));
        }

        @Test
        void refreshThrowsWhenUserDeactivated() {
            sampleUser.setActive(false);
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid-token")
                    .build();

            when(jwtService.hashToken("valid-token")).thenReturn("old-hash");
            when(refreshTokenRepository.findByTokenHash("old-hash"))
                    .thenReturn(Optional.of(validStoredToken));

            assertThrows(UnauthorizedException.class,
                    () -> authService.refresh(request));
        }
    }

    // ── Username generation ─────────────────────────────────────────────

    @Nested
    class UsernameGenerationTests {

        @Test
        void generatesSlugFromName() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);

            String username = authService.generateUniqueUsername("Ahmad Buruh");

            assertNotNull(username);
            // Should start with "ahmad-buruh-"
            assertEquals("ahmad-buruh", username.substring(0, username.lastIndexOf('-')));
        }

        @Test
        void retriesOnCollision() {
            // First attempt collides, second succeeds
            when(userRepository.existsByUsername(anyString()))
                    .thenReturn(true)
                    .thenReturn(false);

            String username = authService.generateUniqueUsername("Test User");

            assertNotNull(username);
        }
    }
}

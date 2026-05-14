package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.request.auth.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.GoogleLoginRequest;
import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.mapper.AuthResponseMapper;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.auth.service.utils.AuthTokenIssuer;
import id.ac.ui.cs.advprog.auth.service.utils.GoogleUserInfo;
import id.ac.ui.cs.advprog.auth.service.utils.UsernameGenerator;
import id.ac.ui.cs.advprog.auth.service.authprovider.AuthProviderFactory;
import id.ac.ui.cs.advprog.auth.service.authprovider.DefaultAuthProviderFactory;
import id.ac.ui.cs.advprog.auth.service.authprovider.GoogleAuthProvider;
import id.ac.ui.cs.advprog.auth.service.authprovider.PasswordAuthProvider;
import id.ac.ui.cs.advprog.auth.service.oauth.OAuthClient;
import id.ac.ui.cs.advprog.auth.validation.RegistrationValidator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
        @Mock private JwtService jwtService;
        @Mock private OAuthClient oauthClient;
        @Mock private PasswordEncoder passwordEncoder;

        private AuthServiceImpl authService;
    private MeterRegistry meterRegistry;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        RegistrationValidator registrationValidator = new RegistrationValidator(userRepository);
        UsernameGenerator usernameGenerator = new UsernameGenerator(userRepository);
        AuthTokenIssuer authTokenIssuer = new AuthTokenIssuer(jwtService);
        AuthResponseMapper authResponseMapper = new AuthResponseMapper();
        meterRegistry = new SimpleMeterRegistry();

        AuthProviderFactory authProviderFactory = new DefaultAuthProviderFactory(
                List.of(
                        new PasswordAuthProvider(userRepository, passwordEncoder),
                        new GoogleAuthProvider(userRepository, oauthClient, usernameGenerator)
                )
        );

        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                registrationValidator,
                usernameGenerator,
                authTokenIssuer,
                authProviderFactory,
                authResponseMapper,
                meterRegistry
        );

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
            when(jwtService.getAccessTokenExpiration()).thenReturn(21600L);

            LoginResponseData result = authService.login(request);

            assertEquals("access-jwt", result.getAccessToken());
            assertEquals("Bearer", result.getTokenType());
            assertEquals(21600, result.getExpiresIn());
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

    @Nested
    class GoogleLoginTests {

        @Test
        void loginWithGoogleCreatesUserWhenNotFound() {
            GoogleLoginRequest request = GoogleLoginRequest.builder()
                    .authorizationCode("google-auth-code")
                    .redirectUri("postmessage")
                    .build();
            GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                    "google-sub-123",
                    "google.user@example.com",
                    "Google User"
            );

            when(oauthClient.authenticate("google-auth-code", "postmessage"))
                    .thenReturn(googleUserInfo);
            when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "google-sub-123"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("google.user@example.com"))
                    .thenReturn(Optional.empty());
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-jwt");
            when(jwtService.getAccessTokenExpiration()).thenReturn(21600L);

            LoginResponseData result = authService.loginWithGoogle(request);

            assertEquals("access-jwt", result.getAccessToken());
            verify(userRepository).save(any(User.class));
        }

        @Test
        void loginWithGoogleLinksExistingEmailUser() {
            GoogleLoginRequest request = GoogleLoginRequest.builder()
                    .authorizationCode("google-auth-code")
                    .redirectUri("postmessage")
                    .build();
            GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                    "google-sub-999",
                    "ahmad@example.com",
                    "Ahmad Buruh"
            );

            sampleUser.setOauthProvider(null);
            sampleUser.setOauthProviderId(null);

            when(oauthClient.authenticate("google-auth-code", "postmessage"))
                    .thenReturn(googleUserInfo);
            when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "google-sub-999"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("ahmad@example.com"))
                    .thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-jwt");
            when(jwtService.getAccessTokenExpiration()).thenReturn(21600L);

            LoginResponseData result = authService.loginWithGoogle(request);

            assertEquals("access-jwt", result.getAccessToken());
            assertEquals("GOOGLE", sampleUser.getOauthProvider());
            assertEquals("google-sub-999", sampleUser.getOauthProviderId());
            verify(userRepository).save(sampleUser);
        }

        @Test
        void loginWithGoogleUsesExistingProviderUser() {
            GoogleLoginRequest request = GoogleLoginRequest.builder()
                    .authorizationCode("google-auth-code")
                    .redirectUri("postmessage")
                    .build();
            sampleUser.setOauthProvider("GOOGLE");
            sampleUser.setOauthProviderId("google-sub-existing");

            when(oauthClient.authenticate("google-auth-code", "postmessage"))
                    .thenReturn(new GoogleUserInfo("google-sub-existing", "ahmad@example.com", "Ahmad"));
            when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "google-sub-existing"))
                    .thenReturn(Optional.of(sampleUser));

            when(jwtService.generateAccessToken(sampleUser)).thenReturn("access-jwt");
            when(jwtService.getAccessTokenExpiration()).thenReturn(21600L);

            LoginResponseData result = authService.loginWithGoogle(request);

            assertEquals("access-jwt", result.getAccessToken());
            verify(userRepository, never()).save(sampleUser);
        }

        @Test
        void loginWithGoogleThrowsWhenLinkedProviderDifferent() {
            GoogleLoginRequest request = GoogleLoginRequest.builder()
                    .authorizationCode("google-auth-code")
                    .redirectUri("postmessage")
                    .build();
            sampleUser.setOauthProvider("GITHUB");
            sampleUser.setOauthProviderId("github-sub");

            when(oauthClient.authenticate("google-auth-code", "postmessage"))
                    .thenReturn(new GoogleUserInfo("google-sub-1", "ahmad@example.com", "Ahmad"));
            when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "google-sub-1"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("ahmad@example.com"))
                    .thenReturn(Optional.of(sampleUser));

            assertThrows(UnauthorizedException.class,
                    () -> authService.loginWithGoogle(request));
        }

        @Test
        void loginWithGoogleThrowsWhenExistingProviderUserInactive() {
            GoogleLoginRequest request = GoogleLoginRequest.builder()
                    .authorizationCode("google-auth-code")
                    .redirectUri("postmessage")
                    .build();
            sampleUser.setOauthProvider("GOOGLE");
            sampleUser.setOauthProviderId("google-sub-inactive");
            sampleUser.setActive(false);

            when(oauthClient.authenticate("google-auth-code", "postmessage"))
                    .thenReturn(new GoogleUserInfo("google-sub-inactive", "ahmad@example.com", "Ahmad"));
            when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "google-sub-inactive"))
                    .thenReturn(Optional.of(sampleUser));

            assertThrows(UnauthorizedException.class,
                    () -> authService.loginWithGoogle(request));
        }

        @Test
        void loginWithGoogleUsesEmailWhenGoogleNameBlank() {
            GoogleLoginRequest request = GoogleLoginRequest.builder()
                    .authorizationCode("google-auth-code")
                    .redirectUri("postmessage")
                    .build();

            when(oauthClient.authenticate("google-auth-code", "postmessage"))
                    .thenReturn(new GoogleUserInfo("google-sub-blank", "blank@example.com", " "));
            when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "google-sub-blank"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("blank@example.com")).thenReturn(Optional.empty());
            when(userRepository.existsByUsername(anyString())).thenReturn(false);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-jwt");
            when(jwtService.getAccessTokenExpiration()).thenReturn(21600L);

            authService.loginWithGoogle(request);

            assertEquals("blank@example.com", userCaptor.getValue().getName());
            assertEquals(UserRole.BURUH, userCaptor.getValue().getRole());
        }

        @Test
        void loginWithGoogleCreatesMandorWhenRoleIsMandor() {
            GoogleLoginRequest request = GoogleLoginRequest.builder()
                    .authorizationCode("google-auth-code")
                    .redirectUri("postmessage")
                    .role("MANDOR")
                    .mandorCertificationNumber("CERT-12345")
                    .build();

            when(oauthClient.authenticate("google-auth-code", "postmessage"))
                    .thenReturn(new GoogleUserInfo("google-sub-mandor", "mandor@example.com", "Mandor User"));
            when(userRepository.findByOauthProviderAndOauthProviderId("GOOGLE", "google-sub-mandor"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("mandor@example.com")).thenReturn(Optional.empty());
            when(userRepository.existsByUsername(anyString())).thenReturn(false);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-jwt");
            when(jwtService.getAccessTokenExpiration()).thenReturn(21600L);

            authService.loginWithGoogle(request);

            assertEquals(UserRole.MANDOR, userCaptor.getValue().getRole());
            assertEquals("CERT-12345", userCaptor.getValue().getMandorCertificationNumber());
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

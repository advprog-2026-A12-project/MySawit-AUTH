package id.ac.ui.cs.advprog.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.auth.config.SecurityConfig;
import id.ac.ui.cs.advprog.auth.dto.request.auth.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.GoogleLoginRequest;
import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.service.AuthService;
import id.ac.ui.cs.advprog.auth.service.JwtService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    // ── Register ────────────────────────────────────────────────────────

    @Nested
    class RegisterTests {

        @Test
        void registerReturns201OnSuccess() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Ahmad Buruh")
                    .email("ahmad@example.com")
                    .password("SecureP@ss123")
                    .role("BURUH")
                    .build();

            RegisterResponseData responseData = RegisterResponseData.builder()
                    .id(UUID.randomUUID())
                    .username("ahmad-buruh-a1b2")
                    .email("ahmad@example.com")
                    .name("Ahmad Buruh")
                    .role("BURUH")
                    .createdAt(Instant.now())
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(responseData);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("Registration successful"))
                    .andExpect(jsonPath("$.data.email").value("ahmad@example.com"))
                    .andExpect(jsonPath("$.data.role").value("BURUH"));
        }

        @Test
        void registerReturns409OnDuplicateEmail() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Ahmad Buruh")
                    .email("existing@example.com")
                    .password("SecureP@ss123")
                    .role("BURUH")
                    .build();

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new DuplicateUserException("Email"));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.field").value("email"))
                    .andExpect(jsonPath("$.message").value("Email is already registered"));
        }

        @Test
        void registerReturns400OnValidationFailure() throws Exception {
            // Missing required fields
            String invalidJson = "{\"email\":\"bad\",\"password\":\"short\"}";

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.field").exists())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    // ── Login ───────────────────────────────────────────────────────────

    @Nested
    class LoginTests {

        @Test
        void loginReturns200OnSuccess() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("ahmad@example.com")
                    .password("SecureP@ss123")
                    .build();

            LoginResponseData responseData = LoginResponseData.builder()
                    .accessToken("jwt-access-token")
                    .tokenType("Bearer")
                    .expiresIn(21600)
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(responseData);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        void loginReturns401OnBadCredentials() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("ahmad@example.com")
                    .password("WrongP@ss123")
                    .build();

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new UnauthorizedException("Invalid email or password"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.field").doesNotExist())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }
    }

        @Nested
        class GoogleLoginTests {

                @Test
                void loginWithGoogleReturns200OnSuccess() throws Exception {
                        GoogleLoginRequest request = GoogleLoginRequest.builder()
                                        .authorizationCode("google-auth-code")
                                        .redirectUri("postmessage")
                                        .build();

                        LoginResponseData responseData = LoginResponseData.builder()
                                        .accessToken("jwt-access-token")
                                        .tokenType("Bearer")
                                        .expiresIn(21600)
                                        .build();

                        when(authService.loginWithGoogle(any(GoogleLoginRequest.class))).thenReturn(responseData);

                        mockMvc.perform(post("/api/v1/auth/google")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.status").value("success"))
                                        .andExpect(jsonPath("$.message").value("Google login successful"))
                                        .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"));
                }

                @Test
                void loginWithGoogleReturns400WhenCodeMissing() throws Exception {
                        String invalidPayload = "{\"authorizationCode\":\"\"}";

                        mockMvc.perform(post("/api/v1/auth/google")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(invalidPayload))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value("error"))
                                        .andExpect(jsonPath("$.field").exists())
                                        .andExpect(jsonPath("$.message").exists());
                }
        }

    // ── Logout ──────────────────────────────────────────────────────────

    @Nested
    class LogoutTests {

        @Test
        void logoutReturns200OnSuccess() throws Exception {
            doNothing().when(authService).logout();

            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(user("testuser").roles("BURUH")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }

        @Test
        void logoutReturns401WithoutAuth() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}

package id.ac.ui.cs.advprog.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.auth.config.SecurityConfig;
import id.ac.ui.cs.advprog.auth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RefreshTokenRequest;
import id.ac.ui.cs.advprog.auth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.response.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.LoginUserDto;
import id.ac.ui.cs.advprog.auth.dto.response.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.TokenRefreshResponseData;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidTokenException;
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
                    .andExpect(jsonPath("$.status").value("error"));
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
                    .andExpect(jsonPath("$.errors").isArray());
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

            LoginUserDto userDto = LoginUserDto.builder()
                    .id(UUID.randomUUID())
                    .email("ahmad@example.com")
                    .name("Ahmad Buruh")
                    .role("BURUH")
                    .build();

            LoginResponseData responseData = LoginResponseData.builder()
                    .accessToken("jwt-access-token")
                    .refreshToken("raw-refresh-token")
                    .tokenType("Bearer")
                    .expiresIn(900)
                    .user(userDto)
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
                    .andExpect(jsonPath("$.status").value("error"));
        }
    }

    // ── Logout ──────────────────────────────────────────────────────────

    @Nested
    class LogoutTests {

        @Test
        void logoutReturns200OnSuccess() throws Exception {
            LogoutRequest request = LogoutRequest.builder()
                    .refreshToken("some-refresh-token")
                    .build();

            doNothing().when(authService).logout(any(LogoutRequest.class));

            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(user("testuser").roles("BURUH"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }

        @Test
        void logoutReturns401WithoutAuth() throws Exception {
            LogoutRequest request = LogoutRequest.builder()
                    .refreshToken("some-refresh-token")
                    .build();

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Refresh ─────────────────────────────────────────────────────────

    @Nested
    class RefreshTests {

        @Test
        void refreshReturns200OnSuccess() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("old-refresh-token")
                    .build();

            TokenRefreshResponseData responseData = TokenRefreshResponseData.builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .tokenType("Bearer")
                    .expiresIn(900)
                    .build();

            when(authService.refresh(any(RefreshTokenRequest.class)))
                    .thenReturn(responseData);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
        }

        @Test
        void refreshReturns401OnInvalidToken() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            when(authService.refresh(any(RefreshTokenRequest.class)))
                    .thenThrow(new InvalidTokenException("Refresh token not found"));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("error"));
        }
    }
}

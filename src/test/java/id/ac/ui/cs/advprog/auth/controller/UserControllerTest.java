package id.ac.ui.cs.advprog.auth.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.auth.config.SecurityConfig;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.service.JwtService;
import id.ac.ui.cs.advprog.auth.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getUsersReturns200ForAdmin() throws Exception {
        UserSummaryResponseData user = UserSummaryResponseData.builder()
                .id(UUID.randomUUID())
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role("BURUH")
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        UserPageResponseData page = UserPageResponseData.builder()
                .content(List.of(user))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(userService.getUsers(anyInt(), anyInt(), anyString(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].email").value("ahmad@example.com"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getUsersReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .with(user("buruh").roles("BURUH")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Only ADMIN can access this resource"));
    }

    @Test
    void getUsersReturns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

        @Test
        void getUserByIdReturns200ForAdmin() throws Exception {
                UUID userId = UUID.randomUUID();
                UserDetailResponseData detail = UserDetailResponseData.builder()
                                .id(userId)
                                .username("ahmad-buruh-a1b2")
                                .email("ahmad@example.com")
                                .name("Ahmad Buruh")
                                .role("BURUH")
                                .isActive(true)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();

                when(userService.getUserById(userId)).thenReturn(detail);

                mockMvc.perform(get("/api/v1/users/{userId}", userId)
                                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("User detail retrieved successfully"))
                                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                                .andExpect(jsonPath("$.data.email").value("ahmad@example.com"));
        }

        @Test
        void getUserByIdReturns403ForNonAdmin() throws Exception {
                mockMvc.perform(get("/api/v1/users/{userId}", UUID.randomUUID())
                                                .with(user("buruh").roles("BURUH")))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Only ADMIN can access this resource"));
        }

        @Test
        void getUserByIdReturns404WhenMissing() throws Exception {
                UUID userId = UUID.randomUUID();
                when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

                mockMvc.perform(get("/api/v1/users/{userId}", userId)
                                                .with(user("admin").roles("ADMIN")))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("User with id " + userId + " not found"));
        }

    @Test
    void getMyProfileReturns200ForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDetailResponseData detail = UserDetailResponseData.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role("BURUH")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userService.getUserById(userId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(user(userId.toString()).roles("BURUH")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Profile retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(userId.toString()));
    }

    @Test
    void getMyProfileReturns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
package id.ac.ui.cs.advprog.auth.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.auth.config.SecurityConfig;
import id.ac.ui.cs.advprog.auth.dto.request.management.UpdateMyProfileRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.DeletedUserResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UpdatedMyProfileResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

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
                .andExpect(jsonPath("$.message").value("Only ADMIN or MANDOR can access this resource"));
    }

    @Test
    void getUsersReturns200ForMandorWhenRoleSupirTruk() throws Exception {
        UserSummaryResponseData user = UserSummaryResponseData.builder()
                .id(UUID.randomUUID())
                .username("charlie-supir-g7h8")
                .email("charlie@example.com")
                .name("Charlie Supir")
                .role("SUPIR_TRUK")
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

        when(userService.getUsers(anyInt(), anyInt(), anyString(), isNull(), isNull(), eq("SUPIR_TRUK")))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/users?role=SUPIR_TRUK")
                        .with(user(UUID.randomUUID().toString()).roles("MANDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].role").value("SUPIR_TRUK"));
    }

    @Test
    void getUsersReturns403ForMandorWhenRoleIsNotSupirTruk() throws Exception {
        mockMvc.perform(get("/api/v1/users?role=BURUH")
                        .with(user(UUID.randomUUID().toString()).roles("MANDOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("MANDOR can only access users with role SUPIR_TRUK"));
    }

    @Test
    void getUsersReturns403ForMandorWhenRoleIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .with(user(UUID.randomUUID().toString()).roles("MANDOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("MANDOR can only access users with role SUPIR_TRUK"));
    }

    @Test
    void getUsersReturns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

        @Test
        void getAllUsersForMandorReturns200ForMandor() throws Exception {
                UserSummaryResponseData user = UserSummaryResponseData.builder()
                                .id(UUID.randomUUID())
                                .username("ahmad-buruh-a1b2")
                                .email("ahmad@example.com")
                                .name("Ahmad Buruh")
                                .role("BURUH")
                                .isActive(true)
                                .createdAt(Instant.now())
                                .build();

                when(userService.getAllUsersForMandor()).thenReturn(List.of(user));

                mockMvc.perform(get("/api/v1/users/mandor/all")
                                                .with(user(UUID.randomUUID().toString()).roles("MANDOR")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                                .andExpect(jsonPath("$.data[0].email").value("ahmad@example.com"));
        }

        @Test
        void getAllUsersForMandorReturns403ForNonMandor() throws Exception {
                mockMvc.perform(get("/api/v1/users/mandor/all")
                                                .with(user(UUID.randomUUID().toString()).roles("ADMIN")))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Only MANDOR can access this resource"));
        }

        @Test
        void getAllUsersForMandorReturns401WithoutAuth() throws Exception {
                mockMvc.perform(get("/api/v1/users/mandor/all"))
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

        @Test
        void getMyProfileReturns401ForInvalidAuthenticatedUserId() throws Exception {
                mockMvc.perform(get("/api/v1/users/me")
                                                .with(user("not-a-uuid").roles("BURUH")))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Unauthorized"));
        }

                    @Test
                    void deleteUserReturns200ForAdmin() throws Exception {
                        UUID userId = UUID.randomUUID();
                        UUID adminId = UUID.randomUUID();
                        DeletedUserResponseData deleted = DeletedUserResponseData.builder()
                                .id(userId)
                                .email("ahmad@example.com")
                                .name("Ahmad Buruh")
                                .deletedAt(Instant.now())
                                .build();

                        when(userService.deleteUser(eq(userId), eq(adminId))).thenReturn(deleted);

                        mockMvc.perform(delete("/api/v1/users/{userId}", userId)
                                        .with(user(adminId.toString()).roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("User deleted successfully"))
                                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                                .andExpect(jsonPath("$.data.email").value("ahmad@example.com"));
                    }

                    @Test
                    void deleteUserReturns403ForNonAdmin() throws Exception {
                        mockMvc.perform(delete("/api/v1/users/{userId}", UUID.randomUUID())
                                        .with(user(UUID.randomUUID().toString()).roles("BURUH")))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Only ADMIN can access this resource"));
                    }

                    @Test
                    void deleteUserReturns401WithoutAuth() throws Exception {
                        mockMvc.perform(delete("/api/v1/users/{userId}", UUID.randomUUID()))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value("error"));
                    }

                    @Test
                    void deleteUserReturns404WhenTargetMissing() throws Exception {
                        UUID userId = UUID.randomUUID();
                        UUID adminId = UUID.randomUUID();
                        when(userService.deleteUser(eq(userId), eq(adminId))).thenThrow(new UserNotFoundException(userId));

                        mockMvc.perform(delete("/api/v1/users/{userId}", userId)
                                        .with(user(adminId.toString()).roles("ADMIN")))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("User with id " + userId + " not found"));
                    }

                    @Test
                    void deleteUserReturns422WhenAdminDeletesSelf() throws Exception {
                        UUID userId = UUID.randomUUID();
                        when(userService.deleteUser(eq(userId), eq(userId)))
                                .thenThrow(new UnprocessableEntityException("Admin cannot delete their own account"));

                        mockMvc.perform(delete("/api/v1/users/{userId}", userId)
                                        .with(user(userId.toString()).roles("ADMIN")))
                                .andExpect(status().is(422))
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Admin cannot delete their own account"));
                    }

                    @Test
                    void deleteUserReturns401ForInvalidAuthenticatedAdminId() throws Exception {
                        mockMvc.perform(delete("/api/v1/users/{userId}", UUID.randomUUID())
                                        .with(user("not-a-uuid").roles("ADMIN")))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Unauthorized"));
                    }

    @Test
    void updateMyProfileReturns200ForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                .name("Ahmad Buruh Updated")
                .password("NewSecureP@ss456")
                .build();

        UpdatedMyProfileResponseData data = UpdatedMyProfileResponseData.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh Updated")
                .role("BURUH")
                .updatedAt(Instant.now())
                .build();

        when(userService.updateMyProfile(eq(userId), org.mockito.ArgumentMatchers.any(UpdateMyProfileRequest.class)))
                .thenReturn(data);

        mockMvc.perform(put("/api/v1/users/me")
                        .with(user(userId.toString()).roles("BURUH"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Ahmad Buruh Updated"));
    }

    @Test
    void updateMyProfileReturns400OnInvalidPassword() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                .password("short")
                .build();

        mockMvc.perform(put("/api/v1/users/me")
                        .with(user(userId.toString()).roles("BURUH"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

        @Test
        void updateMyProfileReturns401ForInvalidAuthenticatedUserId() throws Exception {
                UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                                .name("Updated")
                                .build();

                mockMvc.perform(put("/api/v1/users/me")
                                                .with(user("not-a-uuid").roles("BURUH"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Unauthorized"));
        }
}
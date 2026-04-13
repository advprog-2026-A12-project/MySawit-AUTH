package id.ac.ui.cs.advprog.auth.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.auth.config.SecurityConfig;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.exception.ForbiddenException;
import id.ac.ui.cs.advprog.auth.service.JwtService;
import id.ac.ui.cs.advprog.auth.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeletedUserController.class)
@Import(SecurityConfig.class)
class DeletedUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getDeletedUsersReturns200ForAdmin() throws Exception {
        UserSummaryResponseData user = UserSummaryResponseData.builder()
                .id(UUID.randomUUID())
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role("BURUH")
                .isActive(false)
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

        when(userService.getDeletedUsers(anyInt(), anyInt(), anyString(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/deletedUsers")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Deleted users retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].email").value("ahmad@example.com"));
    }

    @Test
    void getDeletedUsersReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/deletedUsers")
                        .with(user("buruh").roles("BURUH")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Only ADMIN can access this resource"));
    }

        @Test
        void getDeletedUsersReturns403WhenAuthoritiesNull() throws Exception {
                Authentication auth = org.mockito.Mockito.mock(Authentication.class);
                when(auth.getAuthorities()).thenReturn(null);

                mockMvc.perform(get("/api/v1/deletedUsers").with(authentication(auth)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Access denied"));
        }

        @Test
        void getDeletedUsersDirectCallThrows403WhenAuthenticationNull() {
                DeletedUserController controller = new DeletedUserController(userService);

                assertThrows(ForbiddenException.class,
                                () -> controller.getDeletedUsers(0, 20, "createdAt,desc", null, null, null, null));
        }

    @Test
    void getDeletedUsersReturns401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/deletedUsers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }
}

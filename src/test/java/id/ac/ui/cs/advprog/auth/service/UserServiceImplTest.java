package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void getUsersReturnsMappedPage() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        UserPageResponseData result = userService.getUsers(0, 20, "createdAt,desc", null, null, null);

        assertEquals(1, result.getContent().size());
        assertEquals("ahmad@example.com", result.getContent().getFirst().getEmail());
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals("createdAt: DESC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getUsersUsesProvidedSort() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        userService.getUsers(0, 10, "email,asc", null, null, null);

        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals("email: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getUsersThrowsOnInvalidRole() {
        InvalidUserRequestException ex = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.getUsers(0, 20, "createdAt,desc", null, null, "invalid_role")
        );

        assertEquals("Invalid role filter: invalid_role", ex.getMessage());
    }

    @Test
    void getUsersThrowsOnInvalidPageSize() {
        InvalidUserRequestException ex = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.getUsers(0, 0, "createdAt,desc", null, null, null)
        );

        assertEquals("size must be between 1 and 100", ex.getMessage());
    }
}
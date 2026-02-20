package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.UserRequest;
import id.ac.ui.cs.advprog.auth.dto.UserResponse;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRequest(
            "andi01",
            "andi@example.com",
            "Andi",
            "secret",
            UserRole.Admin
        );
    }

    @Test
    void createUserSuccess() {
        User saved = new User();
        saved.setId(1L);
        saved.setUsername(request.username());
        saved.setEmail(request.email());
        saved.setName(request.name());
        saved.setPassword(request.password());
        saved.setRole(request.role());

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("andi01", response.username());
        assertEquals("andi@example.com", response.email());
        assertEquals("Andi", response.name());
        assertEquals(UserRole.Admin, response.role());
    }

    @Test
    void createUserDuplicateUsername() {
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        DuplicateUserException ex = assertThrows(DuplicateUserException.class, () -> userService.createUser(request));
        assertEquals("username already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserDuplicateEmail() {
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        DuplicateUserException ex = assertThrows(DuplicateUserException.class, () -> userService.createUser(request));
        assertEquals("email already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserRequestNull() {
        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class, () -> userService.createUser(null));
        assertEquals("request body is required", ex.getMessage());
    }

    @Test
    void createUserUsernameBlank() {
        UserRequest invalid = new UserRequest(" ", "mail@test.com", "Name", "secret", UserRole.Admin);

        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class, () -> userService.createUser(invalid));
        assertEquals("username is required", ex.getMessage());
    }

    @Test
    void createUserEmailBlank() {
        UserRequest invalid = new UserRequest("user", "", "Name", "secret", UserRole.Admin);

        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class, () -> userService.createUser(invalid));
        assertEquals("email is required", ex.getMessage());
    }

    @Test
    void createUserNameBlank() {
        UserRequest invalid = new UserRequest("user", "mail@test.com", " ", "secret", UserRole.Admin);

        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class, () -> userService.createUser(invalid));
        assertEquals("name is required", ex.getMessage());
    }

    @Test
    void createUserPasswordBlank() {
        UserRequest invalid = new UserRequest("user", "mail@test.com", "Name", " ", UserRole.Admin);

        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class, () -> userService.createUser(invalid));
        assertEquals("password is required", ex.getMessage());
    }

    @Test
    void createUserRoleNull() {
        UserRequest invalid = new UserRequest("user", "mail@test.com", "Name", "secret", null);

        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class, () -> userService.createUser(invalid));
        assertEquals("role is required", ex.getMessage());
    }

    @Test
    void getAllUsersSuccess() {
        User user = new User();
        user.setId(10L);
        user.setUsername("u1");
        user.setEmail("u1@mail.com");
        user.setName("User One");
        user.setPassword("pw");
        user.setRole(UserRole.Buruh);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> responses = userService.getAllUsers();

        assertEquals(1, responses.size());
        assertEquals(10L, responses.getFirst().id());
        assertEquals(UserRole.Buruh, responses.getFirst().role());
    }

    @Test
    void getUserByIdSuccess() {
        User user = new User();
        user.setId(7L);
        user.setUsername("mandor");
        user.setEmail("mandor@mail.com");
        user.setName("Mandor");
        user.setPassword("pw");
        user.setRole(UserRole.Mandor);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(7L);

        assertEquals(7L, response.id());
        assertEquals("mandor", response.username());
    }

    @Test
    void getUserByIdNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
        assertEquals("User with id 99 not found", ex.getMessage());
    }

    @Test
    void updateUserSuccess() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("old");
        existing.setEmail("old@mail.com");
        existing.setName("Old");
        existing.setPassword("oldpw");
        existing.setRole(UserRole.Buruh);

        User updated = new User();
        updated.setId(1L);
        updated.setUsername(request.username());
        updated.setEmail(request.email());
        updated.setName(request.name());
        updated.setPassword(request.password());
        updated.setRole(request.role());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(updated);

        UserResponse response = userService.updateUser(1L, request);

        assertEquals(1L, response.id());
        assertEquals("andi01", response.username());
        assertEquals(UserRole.Admin, response.role());
    }

    @Test
    void updateUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, request));
        assertEquals("User with id 1 not found", ex.getMessage());
    }

    @Test
    void updateUserDuplicateUsername() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("old");
        existing.setEmail("old@mail.com");
        existing.setName("Old");
        existing.setPassword("oldpw");
        existing.setRole(UserRole.Buruh);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        DuplicateUserException ex = assertThrows(DuplicateUserException.class, () -> userService.updateUser(1L, request));
        assertEquals("username already exists", ex.getMessage());
    }

    @Test
    void updateUserDuplicateEmail() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername(request.username());
        existing.setEmail("old@mail.com");
        existing.setName("Old");
        existing.setPassword("oldpw");
        existing.setRole(UserRole.Buruh);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        DuplicateUserException ex = assertThrows(DuplicateUserException.class, () -> userService.updateUser(1L, request));
        assertEquals("email already exists", ex.getMessage());
    }

    @Test
    void updateUserWithSameUsernameAndEmailSkipsDuplicateChecks() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername(request.username());
        existing.setEmail(request.email());
        existing.setName("Old");
        existing.setPassword("oldpw");
        existing.setRole(UserRole.Buruh);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserResponse response = userService.updateUser(1L, request);

        assertEquals(1L, response.id());
        assertEquals(request.username(), response.username());
        verify(userRepository, never()).existsByUsername(request.username());
        verify(userRepository, never()).existsByEmail(request.email());
    }

    @Test
    void deleteUserSuccess() {
        User user = new User();
        user.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        userService.deleteUser(5L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> userService.deleteUser(5L));
        assertTrue(ex.getMessage().contains("5"));
    }
}

package id.ac.ui.cs.advprog.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminAccountInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminAccountInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new AdminAccountInitializer(userRepository, passwordEncoder);
    }

    @Test
    void runSkipsWhenAdminEmailAlreadyExists() throws Exception {
        when(userRepository.existsByEmail("admin@mysawit.local")).thenReturn(true);
        ReflectionTestUtils.setField(initializer, "adminInitialSecret", "secret-from-test");

        initializer.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void runSeedsAdminWithPreferredUsernameWhenAvailable() throws Exception {
        when(userRepository.existsByEmail("admin@mysawit.local")).thenReturn(false);
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("secret-from-test")).thenReturn("encoded-admin-password");
        ReflectionTestUtils.setField(initializer, "adminInitialSecret", "secret-from-test");

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("admin", saved.getUsername());
        assertEquals("admin@mysawit.local", saved.getEmail());
        assertEquals("admin", saved.getName());
        assertEquals(UserRole.ADMIN, saved.getRole());
        assertTrue(saved.isActive());
        assertEquals("encoded-admin-password", saved.getPasswordHash());
    }

    @Test
    void runSeedsAdminWithIncrementedUsernameWhenPreferredTaken() throws Exception {
        when(userRepository.existsByEmail("admin@mysawit.local")).thenReturn(false);
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.existsByUsername("admin-1")).thenReturn(true);
        when(userRepository.existsByUsername("admin-2")).thenReturn(false);
        when(passwordEncoder.encode("secret-from-test")).thenReturn("encoded-admin-password");
        ReflectionTestUtils.setField(initializer, "adminInitialSecret", "secret-from-test");

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("admin-2", saved.getUsername());
        assertEquals(UserRole.ADMIN, saved.getRole());
    }

    @Test
    void runSkipsWhenAdminInitialSecretMissing() throws Exception {
        when(userRepository.existsByEmail("admin@mysawit.local")).thenReturn(false);
        ReflectionTestUtils.setField(initializer, "adminInitialSecret", " ");

        initializer.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void runSkipsWhenAdminInitialSecretNull() throws Exception {
        when(userRepository.existsByEmail("admin@mysawit.local")).thenReturn(false);
        ReflectionTestUtils.setField(initializer, "adminInitialSecret", null);

        initializer.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}

package id.ac.ui.cs.advprog.auth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import org.junit.jupiter.api.Test;

class UserModelTest {

    @Test
    void userGettersSettersWork() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setEmail("user1@mail.com");
        user.setName("User One");
        user.setPassword("secret");
        user.setRole(UserRole.Supir);

        assertEquals(1L, user.getId());
        assertEquals("user1", user.getUsername());
        assertEquals("user1@mail.com", user.getEmail());
        assertEquals("User One", user.getName());
        assertEquals("secret", user.getPassword());
        assertEquals(UserRole.Supir, user.getRole());
    }
}

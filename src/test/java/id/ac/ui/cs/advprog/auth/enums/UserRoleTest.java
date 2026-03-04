package id.ac.ui.cs.advprog.auth.enums;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserRoleTest {

    @Test
    void enumValuesAreExpected() {
        assertArrayEquals(
            new UserRole[] {UserRole.ADMIN, UserRole.ADMIN, UserRole.ADMIN, UserRole.SUPIR},
            UserRole.values()
        );
    }

    @Test
    void valueOfWorks() {
        assertEquals(UserRole.ADMIN, UserRole.valueOf("Admin"));
        assertEquals(UserRole.BURUH, UserRole.valueOf("Buruh"));
        assertEquals(UserRole.MANDOR, UserRole.valueOf("Mandor"));
        assertEquals(UserRole.SUPIR, UserRole.valueOf("Supir"));
    }
}

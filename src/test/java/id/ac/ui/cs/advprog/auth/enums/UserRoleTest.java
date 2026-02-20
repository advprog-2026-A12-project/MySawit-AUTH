package id.ac.ui.cs.advprog.auth.enums;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserRoleTest {

    @Test
    void enumValuesAreExpected() {
        assertArrayEquals(
            new UserRole[] {UserRole.Admin, UserRole.Buruh, UserRole.Mandor, UserRole.Supir},
            UserRole.values()
        );
    }

    @Test
    void valueOfWorks() {
        assertEquals(UserRole.Admin, UserRole.valueOf("Admin"));
        assertEquals(UserRole.Buruh, UserRole.valueOf("Buruh"));
        assertEquals(UserRole.Mandor, UserRole.valueOf("Mandor"));
        assertEquals(UserRole.Supir, UserRole.valueOf("Supir"));
    }
}

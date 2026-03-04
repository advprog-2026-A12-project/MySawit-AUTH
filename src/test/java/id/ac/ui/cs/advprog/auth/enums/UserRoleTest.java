package id.ac.ui.cs.advprog.auth.enums;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserRoleTest {

    @Test
    void enumValuesAreExpected() {
        assertArrayEquals(
            new UserRole[] {UserRole.ADMIN, UserRole.BURUH, UserRole.MANDOR, UserRole.SUPIR_TRUK},
            UserRole.values()
        );
    }

    @Test
    void valueOfWorks() {
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
        assertEquals(UserRole.BURUH, UserRole.valueOf("BURUH"));
        assertEquals(UserRole.MANDOR, UserRole.valueOf("MANDOR"));
        assertEquals(UserRole.SUPIR_TRUK, UserRole.valueOf("SUPIR_TRUK"));
    }
}

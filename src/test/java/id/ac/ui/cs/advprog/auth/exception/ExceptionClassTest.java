package id.ac.ui.cs.advprog.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExceptionClassTest {

    @Test
    void duplicateUserExceptionMessage() {
        DuplicateUserException ex = new DuplicateUserException("username");
        assertEquals("username already exists", ex.getMessage());
    }

    @Test
    void invalidUserRequestExceptionMessage() {
        InvalidUserRequestException ex = new InvalidUserRequestException("invalid input");
        assertEquals("invalid input", ex.getMessage());
    }

    @Test
    void userNotFoundExceptionMessage() {
        UserNotFoundException ex = new UserNotFoundException(123L);
        assertEquals("User with id 123 not found", ex.getMessage());
    }
}

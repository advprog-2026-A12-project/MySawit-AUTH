package id.ac.ui.cs.advprog.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;

class ExceptionClassTest {

    @Test
    void duplicateUserExceptionMessage() {
        DuplicateUserException ex = new DuplicateUserException("username");
        assertEquals("username already exists", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
    }

    @Test
    void invalidUserRequestExceptionMessage() {
        InvalidUserRequestException ex = new InvalidUserRequestException("invalid input");
        assertEquals("invalid input", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void userNotFoundExceptionWithUuid() {
        UUID id = UUID.randomUUID();
        UserNotFoundException ex = new UserNotFoundException(id);
        assertEquals("User with id " + id + " not found", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void userNotFoundExceptionWithMessage() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        assertEquals("User not found", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void unauthorizedExceptionMessage() {
        UnauthorizedException ex = new UnauthorizedException("Bad credentials");
        assertEquals("Bad credentials", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
    }

    @Test
    void forbiddenExceptionDefaultMessage() {
        ForbiddenException ex = new ForbiddenException();
        assertEquals("Access denied", ex.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    void forbiddenExceptionCustomMessage() {
        ForbiddenException ex = new ForbiddenException("Insufficient role");
        assertEquals("Insufficient role", ex.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    void unprocessableEntityExceptionMessage() {
        UnprocessableEntityException ex = new UnprocessableEntityException("Admin cannot delete self");
        assertEquals("Admin cannot delete self", ex.getMessage());
        assertEquals(HttpStatus.valueOf(422), ex.getHttpStatus());
    }

    @Test
    void tooManyRequestsExceptionDefaultMessage() {
        TooManyRequestsException ex = new TooManyRequestsException();
        assertEquals("Too many requests, please try again later", ex.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getHttpStatus());
    }

    @Test
    void tooManyRequestsExceptionCustomMessage() {
        TooManyRequestsException ex = new TooManyRequestsException("Rate limit exceeded");
        assertEquals("Rate limit exceeded", ex.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getHttpStatus());
    }

    @Test
    void invalidTokenExceptionDefaultMessage() {
        InvalidTokenException ex = new InvalidTokenException();
        assertEquals("Token is invalid or expired", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
    }

    @Test
    void invalidTokenExceptionCustomMessage() {
        InvalidTokenException ex = new InvalidTokenException("Refresh token revoked");
        assertEquals("Refresh token revoked", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
    }
}

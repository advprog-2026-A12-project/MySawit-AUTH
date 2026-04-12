package id.ac.ui.cs.advprog.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleUserNotFound() {
        UUID id = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new UserNotFoundException(id));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().status());
        assertEquals("User with id " + id + " not found", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleDuplicateUser() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new DuplicateUserException("email"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().status());
        assertEquals("email", response.getBody().field());
        assertEquals("email is already registered", response.getBody().message());
    }

    @Test
    void handleInvalidRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new InvalidUserRequestException("bad data"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().status());
        assertEquals("bad data", response.getBody().message());
    }

    @Test
    void handleUnauthorized() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new UnauthorizedException("Invalid credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    @Test
    void handleForbidden() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new ForbiddenException());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().message());
    }

    @Test
    void handleUnprocessableEntity() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new UnprocessableEntityException("Admin cannot delete self"));

        assertEquals(HttpStatus.valueOf(422), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Admin cannot delete self", response.getBody().message());
    }

    @Test
    void handleTooManyRequests() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new TooManyRequestsException());

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Too many requests, please try again later", response.getBody().message());
    }

    @Test
    void handleInvalidToken() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new InvalidTokenException());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Token is invalid or expired", response.getBody().message());
    }

    @Test
    void handleGenericException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(new RuntimeException("unexpected"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("general", response.getBody().field());
        assertEquals("Internal server error", response.getBody().message());
    }
}

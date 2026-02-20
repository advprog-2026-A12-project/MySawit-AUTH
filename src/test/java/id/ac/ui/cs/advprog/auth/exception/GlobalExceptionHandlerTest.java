package id.ac.ui.cs.advprog.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        ResponseEntity<ApiErrorResponse> response = handler.handleUserNotFound(new UserNotFoundException(42L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("User with id 42 not found", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleDuplicateUser() {
        ResponseEntity<ApiErrorResponse> response = handler.handleDuplicateUser(new DuplicateUserException("email"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("email already exists", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleInvalidRequest() {
        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidRequest(new InvalidUserRequestException("bad data"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("bad data", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }
}

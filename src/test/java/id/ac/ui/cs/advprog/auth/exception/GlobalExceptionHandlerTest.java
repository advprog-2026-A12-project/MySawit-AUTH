package id.ac.ui.cs.advprog.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
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
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new UserNotFoundException(id));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("User with id " + id + " not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleDuplicateUser() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new DuplicateUserException("email"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("email already exists", response.getBody().getMessage());
    }

    @Test
    void handleInvalidRequest() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new InvalidUserRequestException("bad data"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("bad data", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorized() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new UnauthorizedException("Invalid credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleForbidden() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new ForbiddenException());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleUnprocessableEntity() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new UnprocessableEntityException("Admin cannot delete self"));

        assertEquals(HttpStatus.valueOf(422), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Admin cannot delete self", response.getBody().getMessage());
    }

    @Test
    void handleTooManyRequests() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new TooManyRequestsException());

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Too many requests, please try again later", response.getBody().getMessage());
    }

    @Test
    void handleInvalidToken() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleBaseException(new InvalidTokenException());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Token is invalid or expired", response.getBody().getMessage());
    }

    @Test
    void handleGenericException() {
        ResponseEntity<BaseResponse<Void>> response =
                handler.handleGeneral(new RuntimeException("unexpected"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().getMessage());
    }
}

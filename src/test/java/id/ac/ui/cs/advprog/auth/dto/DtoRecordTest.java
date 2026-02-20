package id.ac.ui.cs.advprog.auth.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.ApiErrorResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DtoRecordTest {

    @Test
    void userRequestRecordWorks() {
        UserRequest request = new UserRequest("u", "u@mail.com", "User", "pw", UserRole.Admin);

        assertEquals("u", request.username());
        assertEquals("u@mail.com", request.email());
        assertEquals("User", request.name());
        assertEquals("pw", request.password());
        assertEquals(UserRole.Admin, request.role());
    }

    @Test
    void userResponseRecordWorks() {
        UserResponse response = new UserResponse(3L, "u", "u@mail.com", "User", UserRole.Mandor);

        assertEquals(3L, response.id());
        assertEquals("u", response.username());
        assertEquals("u@mail.com", response.email());
        assertEquals("User", response.name());
        assertEquals(UserRole.Mandor, response.role());
    }

    @Test
    void apiErrorResponseRecordWorks() {
        Instant now = Instant.now();
        ApiErrorResponse response = new ApiErrorResponse("error", 400, now);

        assertEquals("error", response.message());
        assertEquals(400, response.status());
        assertEquals(now, response.timestamp());
        assertNotNull(response.timestamp());
    }
}

package id.ac.ui.cs.advprog.auth.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import id.ac.ui.cs.advprog.auth.dto.request.auth.LoginRequest;
import id.ac.ui.cs.advprog.auth.dto.request.auth.RegisterRequest;
import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
import id.ac.ui.cs.advprog.auth.dto.response.FieldErrorDto;
import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.auth.LoginUserDto;
import id.ac.ui.cs.advprog.auth.dto.response.auth.RegisterResponseData;
import id.ac.ui.cs.advprog.auth.exception.ApiErrorResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DtoRecordTest {

    @Test
    void registerRequestWorks() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Ahmad Buruh")
                .email("ahmad@example.com")
                .password("SecureP@ss123")
                .role("BURUH")
                .mandorCertificationNumber(null)
                .build();

        assertEquals("Ahmad Buruh", request.getName());
        assertEquals("ahmad@example.com", request.getEmail());
        assertEquals("SecureP@ss123", request.getPassword());
        assertEquals("BURUH", request.getRole());
        assertNull(request.getMandorCertificationNumber());
    }

    @Test
    void loginRequestWorks() {
        LoginRequest request = LoginRequest.builder()
                .email("ahmad@example.com")
                .password("SecureP@ss123")
                .build();

        assertEquals("ahmad@example.com", request.getEmail());
        assertEquals("SecureP@ss123", request.getPassword());
    }

    @Test
    void registerResponseDataWorks() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        RegisterResponseData data = RegisterResponseData.builder()
                .id(id)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role("BURUH")
                .createdAt(now)
                .build();

        assertEquals(id, data.getId());
        assertEquals("ahmad-buruh-a1b2", data.getUsername());
        assertEquals("ahmad@example.com", data.getEmail());
        assertEquals("Ahmad Buruh", data.getName());
        assertEquals("BURUH", data.getRole());
        assertEquals(now, data.getCreatedAt());
    }

    @Test
    void loginResponseDataWorks() {
        LoginUserDto user = LoginUserDto.builder()
                .id(UUID.randomUUID())
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role("BURUH")
                .build();

        LoginResponseData data = LoginResponseData.builder()
                .accessToken("access-token")
                .tokenType("Bearer")
            .expiresIn(21600)
                .build();

        assertEquals("access-token", data.getAccessToken());
        assertEquals("Bearer", data.getTokenType());
        assertEquals(21600, data.getExpiresIn());
    }

    @Test
    void baseResponseSuccessWorks() {
        BaseResponse<String> response = BaseResponse.success("OK", "hello");

        assertEquals("success", response.getStatus());
        assertEquals("OK", response.getMessage());
        assertEquals("hello", response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getErrors());
    }

    @Test
    void baseResponseErrorWorks() {
        FieldErrorDto fieldError = FieldErrorDto.builder()
                .field("email")
                .message("Email is already registered")
                .build();

        BaseResponse<Void> response = BaseResponse.error("Validation failed", List.of(fieldError));

        assertEquals("error", response.getStatus());
        assertEquals("Validation failed", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());
        assertEquals("email", response.getErrors().getFirst().getField());
    }

    @Test
    void baseResponseErrorWithoutFieldErrorsWorks() {
        BaseResponse<Void> response = BaseResponse.error("Unauthorized");

        assertEquals("error", response.getStatus());
        assertEquals("Unauthorized", response.getMessage());
        assertNull(response.getData());
        assertNull(response.getErrors());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void fieldErrorDtoWorks() {
        FieldErrorDto dto = new FieldErrorDto("name", "Name is required");

        assertEquals("name", dto.getField());
        assertEquals("Name is required", dto.getMessage());
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

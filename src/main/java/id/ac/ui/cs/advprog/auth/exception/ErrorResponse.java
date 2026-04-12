package id.ac.ui.cs.advprog.auth.exception;

import java.time.Instant;

public record ErrorResponse(
        String status,
        String field,
        String message,
        String timestamp
) {
    public static ErrorResponse of(String field, String message) {
        return new ErrorResponse("error", field, message, Instant.now().toString());
    }
}
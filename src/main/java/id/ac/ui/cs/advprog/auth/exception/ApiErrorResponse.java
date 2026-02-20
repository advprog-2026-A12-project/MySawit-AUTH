package id.ac.ui.cs.advprog.auth.exception;

import java.time.Instant;

public record ApiErrorResponse(
    String message,
    int status,
    Instant timestamp
) {}

package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 401 — Refresh token is expired, invalid, or already revoked.
 */
public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public InvalidTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Token is invalid or expired");
    }
}

package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 400 — Bad request / validation failure.
 */
public class InvalidUserRequestException extends BaseException {

    public InvalidUserRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 429 — Rate limit exceeded.
 */
public class TooManyRequestsException extends BaseException {

    public TooManyRequestsException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    public TooManyRequestsException() {
        super(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later");
    }
}

package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 401 — Authentication failure (bad credentials, expired/invalid token, inactive account).
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}

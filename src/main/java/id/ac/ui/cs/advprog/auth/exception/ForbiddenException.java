package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 403 — Role does not have access to the requested resource.
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN, "Access denied");
    }
}

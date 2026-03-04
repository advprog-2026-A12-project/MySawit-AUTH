package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 422 — Business logic violation (e.g. admin deleting self, mandor missing cert).
 */
public class UnprocessableEntityException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.valueOf(422);

    public UnprocessableEntityException(String message) {
        super(STATUS, message);
    }
}

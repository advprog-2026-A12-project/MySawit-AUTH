package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 409 — Duplicate data conflict (e.g. email/username already registered).
 */
public class DuplicateUserException extends BaseException {

    public DuplicateUserException(String field) {
        super(HttpStatus.CONFLICT, field + " already exists");
    }
}

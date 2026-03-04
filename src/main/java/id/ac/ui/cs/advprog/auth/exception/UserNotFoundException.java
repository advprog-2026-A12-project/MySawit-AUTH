package id.ac.ui.cs.advprog.auth.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

/**
 * 404 — Resource not found.
 */
public class UserNotFoundException extends BaseException {

    public UserNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "User with id " + id + " not found");
    }

    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

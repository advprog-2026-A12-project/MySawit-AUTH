package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

public class AssignmentConflictException extends BaseException {

    public AssignmentConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
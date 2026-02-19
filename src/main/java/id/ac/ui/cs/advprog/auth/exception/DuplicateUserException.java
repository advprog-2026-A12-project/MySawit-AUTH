package id.ac.ui.cs.advprog.auth.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String field) {
        super(field + " already exists");
    }
}

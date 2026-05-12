package id.ac.ui.cs.advprog.auth.validation;

import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import org.springframework.stereotype.Component;

@Component
public class AssignmentQueryValidator {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    public void validatePageParams(int page, int size) {
        if (page < 0) {
            throw new InvalidUserRequestException("page must be greater than or equal to 0");
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new InvalidUserRequestException("size must be between 1 and 100");
        }
    }
}

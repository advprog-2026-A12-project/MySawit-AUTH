package id.ac.ui.cs.advprog.auth.validation;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class UserQueryValidator {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "email", "role", "createdAt");

    public void validatePageParams(int page, int size) {
        if (page < 0) {
            throw new InvalidUserRequestException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new InvalidUserRequestException("size must be between 1 and 100");
        }
    }

    public Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sortParam.split(",");
        if (parts.length != 2) {
            throw new InvalidUserRequestException("Invalid sort format. Use field,direction");
        }

        String field = parts[0].trim();
        String directionRaw = parts[1].trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new InvalidUserRequestException("Invalid sort field: " + field);
        }

        Sort.Direction direction;
        if ("asc".equals(directionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equals(directionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new InvalidUserRequestException("Invalid sort direction: " + directionRaw);
        }

        return Sort.by(direction, field);
    }

    public UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserRequestException("Invalid role filter: " + role);
        }
    }
}

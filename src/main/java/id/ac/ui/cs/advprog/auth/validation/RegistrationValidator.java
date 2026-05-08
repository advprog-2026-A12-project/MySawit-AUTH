package id.ac.ui.cs.advprog.auth.validation;

import id.ac.ui.cs.advprog.auth.dto.request.auth.RegisterRequest;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {

    private final UserRepository userRepository;

    public UserRole validate(RegisterRequest request) {
        UserRole role = parseAndValidateRole(request.getRole());

        validateMandorCertification(role, request.getMandorCertificationNumber());
        validateUniqueEmail(request.getEmail());
        validateUniqueMandorCertification(request.getMandorCertificationNumber());

        return role;
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("Email");
        }
    }

    private void validateUniqueMandorCertification(String certNumber) {
        if (certNumber != null && !certNumber.isBlank()
                && userRepository.existsByMandorCertificationNumber(certNumber)) {
            throw new DuplicateUserException("Mandor certification number");
        }
    }

    private UserRole parseAndValidateRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            throw new InvalidUserRequestException("Invalid role: " + roleStr);
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserRequestException("Invalid role: " + roleStr);
        }

        if (role == UserRole.ADMIN) {
            throw new UnprocessableEntityException("ADMIN role is not allowed for registration");
        }

        return role;
    }

    private void validateMandorCertification(UserRole role, String certNumber) {
        if (role == UserRole.MANDOR && (certNumber == null || certNumber.isBlank())) {
            throw new UnprocessableEntityException(
                    "mandorCertificationNumber is required for MANDOR role");
        }
    }
}

package id.ac.ui.cs.advprog.auth.validation;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.AssignmentConflictException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentValidator {

    private final UserRepository userRepository;
    private final BuruhMandorAssignmentRepository assignmentRepository;

    public User getActiveUser(UUID userId, String notFoundMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(notFoundMessage));
        if (!user.isActive()) {
            throw new UserNotFoundException(notFoundMessage);
        }
        return user;
    }

    public void requireRole(User user, UserRole expectedRole, String message) {
        if (user.getRole() != expectedRole) {
            throw new UnprocessableEntityException(message);
        }
    }

    public void ensureNotAssigned(UUID buruhId) {
        if (assignmentRepository.existsByBuruhIdAndIsActiveTrue(buruhId)) {
            throw new AssignmentConflictException("Buruh already has an active assignment");
        }
    }

    public User requireActiveBuruh(BuruhMandorAssignment assignment) {
        User buruh = assignment.getBuruh();
        if (!buruh.isActive() || buruh.getRole() != UserRole.BURUH) {
            throw new UserNotFoundException("Buruh not found or has no active assignment");
        }
        return buruh;
    }
}

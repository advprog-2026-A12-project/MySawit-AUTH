package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.AssignmentUserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.ReassignmentUserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.AssignmentConflictException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import id.ac.ui.cs.advprog.auth.model.User;
import java.time.Instant;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final BuruhMandorAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BuruhMandorAssignmentResponseData assignBuruhToMandor(AssignBuruhMandorRequest request) {
        User buruh = getActiveUser(request.getBuruhId(), "Buruh not found");
        User mandor = getActiveUser(request.getMandorId(), "Mandor not found");

        if (buruh.getRole() != UserRole.BURUH) {
            throw new UnprocessableEntityException("Provided buruhId is not a BURUH user");
        }
        if (mandor.getRole() != UserRole.MANDOR) {
            throw new UnprocessableEntityException("Provided mandorId is not a MANDOR user");
        }

        if (assignmentRepository.existsByBuruhIdAndIsActiveTrue(buruh.getId())) {
            throw new AssignmentConflictException("Buruh already has an active assignment");
        }

        BuruhMandorAssignment assignment = BuruhMandorAssignment.builder()
                .buruh(buruh)
                .mandor(mandor)
                .isActive(true)
                .build();

        BuruhMandorAssignment saved = assignmentRepository.save(assignment);
        return BuruhMandorAssignmentResponseData.builder()
                .id(saved.getId())
                .buruh(toAssignmentUserSummary(buruh))
                .mandor(toAssignmentUserSummary(mandor))
                .assignedAt(saved.getAssignedAt())
                .build();
    }

    @Override
    @Transactional
    public BuruhMandorReassignmentResponseData reassignBuruhToMandor(UUID buruhId, ReassignBuruhMandorRequest request) {
        BuruhMandorAssignment activeAssignment = assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)
                .orElseThrow(() -> new UserNotFoundException("Buruh not found or has no active assignment"));

        User buruh = activeAssignment.getBuruh();
        if (!buruh.isActive() || buruh.getRole() != UserRole.BURUH) {
            throw new UserNotFoundException("Buruh not found or has no active assignment");
        }

        User previousMandor = activeAssignment.getMandor();
        User newMandor = getActiveUser(request.getNewMandorId(), "New mandor not found");

        if (newMandor.getRole() != UserRole.MANDOR) {
            throw new UnprocessableEntityException("Provided newMandorId is not a MANDOR user");
        }

        if (previousMandor.getId().equals(newMandor.getId())) {
            throw new UnprocessableEntityException("New mandor must be different from current mandor");
        }

        activeAssignment.setActive(false);
        activeAssignment.setUnassignedAt(Instant.now());
        assignmentRepository.save(activeAssignment);

        BuruhMandorAssignment newAssignment = BuruhMandorAssignment.builder()
                .buruh(buruh)
                .mandor(newMandor)
                .isActive(true)
                .build();

        BuruhMandorAssignment saved = assignmentRepository.save(newAssignment);

        return BuruhMandorReassignmentResponseData.builder()
                .buruh(toReassignmentUserSummary(buruh))
                .previousMandor(toReassignmentUserSummary(previousMandor))
                .newMandor(toReassignmentUserSummary(newMandor))
                .reassignedAt(saved.getAssignedAt())
                .build();
    }

    private User getActiveUser(UUID userId, String notFoundMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(notFoundMessage));
        if (!user.isActive()) {
            throw new UserNotFoundException(notFoundMessage);
        }
        return user;
    }

    private AssignmentUserSummaryResponseData toAssignmentUserSummary(User user) {
        return AssignmentUserSummaryResponseData.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private ReassignmentUserSummaryResponseData toReassignmentUserSummary(User user) {
        return ReassignmentUserSummaryResponseData.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
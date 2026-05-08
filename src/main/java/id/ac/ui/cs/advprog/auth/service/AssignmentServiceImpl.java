package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorUnassignmentResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.mapper.AssignmentResponseMapper;
import id.ac.ui.cs.advprog.auth.mapper.AssignmentSpecificationBuilder;
import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import id.ac.ui.cs.advprog.auth.model.User;
import java.time.Instant;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import id.ac.ui.cs.advprog.auth.validation.AssignmentValidator;
import id.ac.ui.cs.advprog.auth.validation.AssignmentQueryValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final BuruhMandorAssignmentRepository assignmentRepository;
    private final AssignmentQueryValidator assignmentQueryValidator;
    private final AssignmentValidator assignmentPolicy;
    private final AssignmentSpecificationBuilder assignmentSpecificationBuilder;
    private final AssignmentResponseMapper assignmentResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public BuruhMandorAssignmentPageResponseData getAssignments(
        int page,
        int size,
        UUID mandorId,
        String buruhName,
        String mandorName
    ) {
        assignmentQueryValidator.validatePageParams(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "assignedAt"));
        Specification<BuruhMandorAssignment> specification =
            assignmentSpecificationBuilder.build(mandorId, buruhName, mandorName);
        Page<BuruhMandorAssignment> assignmentsPage = assignmentRepository.findAll(specification, pageable);

        List<BuruhMandorAssignmentResponseData> content = assignmentsPage.getContent().stream()
            .map(assignmentResponseMapper::toAssignmentResponse)
            .toList();

        return assignmentResponseMapper.toPageResponse(assignmentsPage, content);
    }

    @Override
    @Transactional
    public BuruhMandorAssignmentResponseData assignBuruhToMandor(AssignBuruhMandorRequest request) {
        User buruh = assignmentPolicy.getActiveUser(request.getBuruhId(), "Buruh not found");
        User mandor = assignmentPolicy.getActiveUser(request.getMandorId(), "Mandor not found");

        assignmentPolicy.requireRole(buruh, UserRole.BURUH, "Provided buruhId is not a BURUH user");
        assignmentPolicy.requireRole(mandor, UserRole.MANDOR, "Provided mandorId is not a MANDOR user");
        assignmentPolicy.ensureNotAssigned(buruh.getId());

        BuruhMandorAssignment assignment = BuruhMandorAssignment.builder()
                .buruh(buruh)
                .mandor(mandor)
                .isActive(true)
                .build();

        BuruhMandorAssignment saved = assignmentRepository.save(assignment);
        return assignmentResponseMapper.toAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public BuruhMandorReassignmentResponseData reassignBuruhToMandor(UUID buruhId, ReassignBuruhMandorRequest request) {
        BuruhMandorAssignment activeAssignment = assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)
                .orElseThrow(() -> new UserNotFoundException("Buruh not found or has no active assignment"));

        User buruh = assignmentPolicy.requireActiveBuruh(activeAssignment);

        User previousMandor = activeAssignment.getMandor();
        User newMandor = assignmentPolicy.getActiveUser(request.getNewMandorId(), "New mandor not found");

        assignmentPolicy.requireRole(newMandor, UserRole.MANDOR, "Provided newMandorId is not a MANDOR user");

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
            .buruh(assignmentResponseMapper.toReassignmentUserSummary(buruh))
            .previousMandor(assignmentResponseMapper.toReassignmentUserSummary(previousMandor))
            .newMandor(assignmentResponseMapper.toReassignmentUserSummary(newMandor))
                .reassignedAt(saved.getAssignedAt())
                .build();
    }

    @Override
    @Transactional
    public BuruhMandorUnassignmentResponseData unassignBuruhFromMandor(UUID buruhId) {
        BuruhMandorAssignment activeAssignment = assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)
                .orElseThrow(() -> new UserNotFoundException("Buruh not found or has no active assignment"));

        User buruh = assignmentPolicy.requireActiveBuruh(activeAssignment);

        User previousMandor = activeAssignment.getMandor();
        Instant unassignedAt = Instant.now();

        activeAssignment.setActive(false);
        activeAssignment.setUnassignedAt(unassignedAt);
        assignmentRepository.save(activeAssignment);

        return BuruhMandorUnassignmentResponseData.builder()
            .buruh(assignmentResponseMapper.toReassignmentUserSummary(buruh))
            .previousMandor(assignmentResponseMapper.toReassignmentUserSummary(previousMandor))
                .unassignedAt(unassignedAt)
                .build();
    }

}
package id.ac.ui.cs.advprog.auth.mapper;

import id.ac.ui.cs.advprog.auth.dto.response.management.AssignmentUserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorUnassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.ReassignmentUserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import id.ac.ui.cs.advprog.auth.model.User;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class AssignmentResponseMapper {

    public AssignmentUserSummaryResponseData toAssignmentUserSummary(User user) {
        return AssignmentUserSummaryResponseData.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public BuruhMandorAssignmentResponseData toAssignmentResponse(BuruhMandorAssignment assignment) {
        return BuruhMandorAssignmentResponseData.builder()
                .id(assignment.getId())
                .buruh(toAssignmentUserSummary(assignment.getBuruh()))
                .mandor(toAssignmentUserSummary(assignment.getMandor()))
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    public BuruhMandorAssignmentPageResponseData toPageResponse(
            Page<BuruhMandorAssignment> assignmentsPage,
            List<BuruhMandorAssignmentResponseData> content
    ) {
        return BuruhMandorAssignmentPageResponseData.builder()
                .content(content)
                .page(assignmentsPage.getNumber())
                .size(assignmentsPage.getSize())
                .totalElements(assignmentsPage.getTotalElements())
                .totalPages(assignmentsPage.getTotalPages())
                .first(assignmentsPage.isFirst())
                .last(assignmentsPage.isLast())
                .build();
    }

    public ReassignmentUserSummaryResponseData toReassignmentUserSummary(User user) {
        return ReassignmentUserSummaryResponseData.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

        public BuruhMandorReassignmentResponseData toReassignmentResponse(
            User buruh,
            User previousMandor,
            User newMandor,
            Instant reassignedAt
        ) {
        return BuruhMandorReassignmentResponseData.builder()
            .buruh(toReassignmentUserSummary(buruh))
            .previousMandor(toReassignmentUserSummary(previousMandor))
            .newMandor(toReassignmentUserSummary(newMandor))
            .reassignedAt(reassignedAt)
            .build();
        }

        public BuruhMandorUnassignmentResponseData toUnassignmentResponse(
            User buruh,
            User previousMandor,
            Instant unassignedAt
        ) {
        return BuruhMandorUnassignmentResponseData.builder()
            .buruh(toReassignmentUserSummary(buruh))
            .previousMandor(toReassignmentUserSummary(previousMandor))
            .unassignedAt(unassignedAt)
            .build();
        }
}

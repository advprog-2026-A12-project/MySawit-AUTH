package id.ac.ui.cs.advprog.auth.controller;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorUnassignmentResponseData;
import id.ac.ui.cs.advprog.auth.exception.ForbiddenException;
import id.ac.ui.cs.advprog.auth.service.AssignmentService;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/buruh-mandor")
    public ResponseEntity<BaseResponse<BuruhMandorAssignmentResponseData>> assignBuruhToMandor(
            @Valid @RequestBody AssignBuruhMandorRequest request,
            Authentication authentication
    ) {
        enforceAdminOnly(authentication);

        BuruhMandorAssignmentResponseData data = assignmentService.assignBuruhToMandor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Buruh assigned to Mandor successfully", data));
    }

    @PutMapping("/buruh-mandor/{buruhId}")
    public ResponseEntity<BaseResponse<BuruhMandorReassignmentResponseData>> reassignBuruhToMandor(
            @PathVariable UUID buruhId,
            @Valid @RequestBody ReassignBuruhMandorRequest request,
            Authentication authentication
    ) {
        enforceAdminOnly(authentication);

        BuruhMandorReassignmentResponseData data = assignmentService.reassignBuruhToMandor(buruhId, request);
        return ResponseEntity.ok(BaseResponse.success("Buruh reassigned successfully", data));
    }

    @DeleteMapping("/buruh-mandor/{buruhId}")
    public ResponseEntity<BaseResponse<BuruhMandorUnassignmentResponseData>> unassignBuruhFromMandor(
            @PathVariable UUID buruhId,
            Authentication authentication
    ) {
        enforceAdminOnly(authentication);

        BuruhMandorUnassignmentResponseData data = assignmentService.unassignBuruhFromMandor(buruhId);
        return ResponseEntity.ok(BaseResponse.success("Buruh unassigned from Mandor successfully", data));
    }

    private void enforceAdminOnly(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new ForbiddenException();
        }

        boolean hasAdminRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (!hasAdminRole) {
            throw new ForbiddenException("Only ADMIN can access this resource");
        }
    }
}
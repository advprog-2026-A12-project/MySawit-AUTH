package id.ac.ui.cs.advprog.auth.controller;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.exception.ForbiddenException;
import id.ac.ui.cs.advprog.auth.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
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
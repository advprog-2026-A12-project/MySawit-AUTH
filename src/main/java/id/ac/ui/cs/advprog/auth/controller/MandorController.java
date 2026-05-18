package id.ac.ui.cs.advprog.auth.controller;

import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
import id.ac.ui.cs.advprog.auth.dto.response.management.MandorBuruhPageResponseData;
import id.ac.ui.cs.advprog.auth.security.UserListAccessPolicy;
import id.ac.ui.cs.advprog.auth.service.AssignmentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mandors")
@RequiredArgsConstructor
@Validated
public class MandorController {

    private final AssignmentService assignmentService;
    private final UserListAccessPolicy userListAccessPolicy;

    @GetMapping("/{mandorId}/buruhs")
    @PreAuthorize("hasAnyRole('ADMIN','MANDOR')")
    public ResponseEntity<BaseResponse<MandorBuruhPageResponseData>> getBuruhsByMandor(
            @PathVariable UUID mandorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            Authentication authentication
    ) {
        userListAccessPolicy.verifyMandorScope(authentication, mandorId);
        MandorBuruhPageResponseData data = assignmentService.getBuruhsByMandor(mandorId, page, size, name);
        return ResponseEntity.ok(BaseResponse.success("Buruhs retrieved successfully", data));
    }
}

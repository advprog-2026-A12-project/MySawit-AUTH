package id.ac.ui.cs.advprog.auth.controller;

import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.exception.ForbiddenException;
import id.ac.ui.cs.advprog.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deletedUsers")
@RequiredArgsConstructor
@Validated
public class DeletedUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<BaseResponse<UserPageResponseData>> getDeletedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            Authentication authentication
    ) {
        enforceAdminOnly(authentication);

        UserPageResponseData data = userService.getDeletedUsers(page, size, sort, name, email, role);
        return ResponseEntity.ok(BaseResponse.success("Deleted users retrieved successfully", data));
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

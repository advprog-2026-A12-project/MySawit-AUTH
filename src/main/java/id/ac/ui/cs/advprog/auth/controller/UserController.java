package id.ac.ui.cs.advprog.auth.controller;

import id.ac.ui.cs.advprog.auth.dto.request.management.UpdateMyProfileRequest;
import id.ac.ui.cs.advprog.auth.dto.response.BaseResponse;
import id.ac.ui.cs.advprog.auth.dto.response.management.UpdatedMyProfileResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.exception.ForbiddenException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import id.ac.ui.cs.advprog.auth.service.UserService;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<BaseResponse<UserPageResponseData>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            Authentication authentication
    ) {
        enforceAdminOnly(authentication);

        UserPageResponseData data = userService.getUsers(page, size, sort, name, email, role);
        return ResponseEntity.ok(BaseResponse.success("Users retrieved successfully", data));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse<UserDetailResponseData>> getUserById(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        enforceAdminOnly(authentication);

        UserDetailResponseData data = userService.getUserById(userId);
        return ResponseEntity.ok(BaseResponse.success("User detail retrieved successfully", data));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDetailResponseData>> getMyProfile(Authentication authentication) {
        UUID userId = extractAuthenticatedUserId(authentication);
        UserDetailResponseData data = userService.getUserById(userId);
        return ResponseEntity.ok(BaseResponse.success("Profile retrieved successfully", data));
    }

    @PutMapping("/me")
    public ResponseEntity<BaseResponse<UpdatedMyProfileResponseData>> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request,
            Authentication authentication
    ) {
        UUID userId = extractAuthenticatedUserId(authentication);
        UpdatedMyProfileResponseData data = userService.updateMyProfile(userId, request);
        return ResponseEntity.ok(BaseResponse.success("Profile updated successfully", data));
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

    private UUID extractAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}
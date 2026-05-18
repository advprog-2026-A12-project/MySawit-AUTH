package id.ac.ui.cs.advprog.auth.security;

import id.ac.ui.cs.advprog.auth.exception.ForbiddenException;
import id.ac.ui.cs.advprog.auth.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserListAccessPolicy {

    public String resolveRoleFilter(Authentication authentication, String role) {
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new ForbiddenException();
        }

        boolean hasAdminRole = hasRole(authentication, "ROLE_ADMIN");

        if (hasAdminRole) {
            return role;
        }

        boolean hasMandorRole = hasRole(authentication, "ROLE_MANDOR");

        if (!hasMandorRole) {
            throw new ForbiddenException("Only ADMIN or MANDOR can access this resource");
        }

        if (!"SUPIR_TRUK".equalsIgnoreCase(role)) {
            throw new ForbiddenException("MANDOR can only access users with role SUPIR_TRUK");
        }

        return "SUPIR_TRUK";
    }

    public UUID extractAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

    public void verifyMandorScope(Authentication authentication, UUID mandorId) {
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new ForbiddenException();
        }

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }

        if (!hasRole(authentication, "ROLE_MANDOR")) {
            throw new ForbiddenException("Only ADMIN or MANDOR can access this resource");
        }

        UUID authenticatedUserId = extractAuthenticatedUserId(authentication);
        if (!authenticatedUserId.equals(mandorId)) {
            throw new ForbiddenException("MANDOR can only access their own buruh assignments");
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}

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

        boolean hasAdminRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (hasAdminRole) {
            return role;
        }

        boolean hasMandorRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_MANDOR"::equals);

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
}

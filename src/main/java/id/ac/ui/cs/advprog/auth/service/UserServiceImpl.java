package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.UpdateMyProfileRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.DeletedUserResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UpdatedMyProfileResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import id.ac.ui.cs.advprog.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.time.Instant;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "email", "role", "createdAt");

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BuruhMandorAssignmentRepository buruhMandorAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserPageResponseData getUsers(int page, int size, String sort, String name, String email, String role) {
        validatePageParams(page, size);

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Specification<User> specification = buildSpecification(name, email, role, true);

        Page<User> usersPage = userRepository.findAll(specification, pageable);

        List<UserSummaryResponseData> content = usersPage.getContent().stream()
            .map(this::toSummaryResponse)
            .toList();

        return UserPageResponseData.builder()
            .content(content)
            .page(usersPage.getNumber())
            .size(usersPage.getSize())
            .totalElements(usersPage.getTotalElements())
            .totalPages(usersPage.getTotalPages())
            .first(usersPage.isFirst())
            .last(usersPage.isLast())
            .build();
        }

        @Override
        @Transactional(readOnly = true)
        public UserPageResponseData getDeletedUsers(int page, int size, String sort, String name, String email, String role) {
        validatePageParams(page, size);

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Specification<User> specification = buildSpecification(name, email, role, false);

        Page<User> usersPage = userRepository.findAll(specification, pageable);

        List<UserSummaryResponseData> content = usersPage.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();

        return UserPageResponseData.builder()
                .content(content)
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .first(usersPage.isFirst())
                .last(usersPage.isLast())
                .build();
    }

            @Override
            @Transactional(readOnly = true)
            public UserDetailResponseData getUserById(UUID userId) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

            Map<String, Object> roleSpecificData = buildRoleSpecificData(user);

            return UserDetailResponseData.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .mandorCertificationNumber(user.getMandorCertificationNumber())
                .isActive(user.isActive())
                .oauthProvider(user.getOauthProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roleSpecificData(roleSpecificData)
                .build();
            }

    private Map<String, Object> buildRoleSpecificData(User user) {
        if (user.getRole() == UserRole.BURUH) {
            return buruhMandorAssignmentRepository.findByBuruhIdAndIsActiveTrue(user.getId())
                    .map(assignment -> {
                        Map<String, Object> assignedMandor = new HashMap<>();
                        assignedMandor.put("id", assignment.getMandor().getId());
                        assignedMandor.put("name", assignment.getMandor().getName());
                        assignedMandor.put("email", assignment.getMandor().getEmail());

                        Map<String, Object> roleSpecificData = new HashMap<>();
                        roleSpecificData.put("assignedMandor", assignedMandor);
                        roleSpecificData.put("assignedAt", assignment.getAssignedAt());
                        return roleSpecificData;
                    })
                    .orElseGet(Map::of);
        }

        if (user.getRole() == UserRole.MANDOR) {
            List<Map<String, Object>> assignedBuruhs = buruhMandorAssignmentRepository
                    .findAllByMandor_IdAndIsActiveTrue(user.getId(), Sort.by(Sort.Direction.DESC, "assignedAt"))
                    .stream()
                    .map(assignment -> {
                        Map<String, Object> assignedBuruh = new HashMap<>();
                        assignedBuruh.put("id", assignment.getBuruh().getId());
                        assignedBuruh.put("name", assignment.getBuruh().getName());
                        assignedBuruh.put("email", assignment.getBuruh().getEmail());
                        assignedBuruh.put("assignedAt", assignment.getAssignedAt());
                        return assignedBuruh;
                    })
                    .toList();

            Map<String, Object> roleSpecificData = new HashMap<>();
            roleSpecificData.put("assignedBuruhs", assignedBuruhs);
            roleSpecificData.put("totalAssignedBuruhs", assignedBuruhs.size());
            return roleSpecificData;
        }

        return Map.of();
    }

    @Override
    @Transactional
    public DeletedUserResponseData deleteUser(UUID userId, UUID authenticatedAdminId) {
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (userId.equals(authenticatedAdminId)) {
            throw new UnprocessableEntityException("Admin cannot delete their own account");
        }

        user.setActive(false);
        Instant deletedAt = Instant.now();
        user.setUpdatedAt(deletedAt);

        User saved = userRepository.save(user);
        refreshTokenRepository.deleteAllByUser(saved);

        return DeletedUserResponseData.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .deletedAt(deletedAt)
                .build();
    }

    @Override
    @Transactional
    public UpdatedMyProfileResponseData updateMyProfile(UUID userId, UpdateMyProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean hasNameUpdate = request.getName() != null && !request.getName().isBlank();
        boolean hasPasswordUpdate = request.getPassword() != null && !request.getPassword().isBlank();

        if (!hasNameUpdate && !hasPasswordUpdate) {
            throw new InvalidUserRequestException("At least one of name or password must be provided");
        }

        if (hasNameUpdate) {
            user.setName(request.getName());
        }

        if (hasPasswordUpdate) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);

        return UpdatedMyProfileResponseData.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .name(saved.getName())
                .role(saved.getRole().name())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    private void validatePageParams(int page, int size) {
        if (page < 0) {
            throw new InvalidUserRequestException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new InvalidUserRequestException("size must be between 1 and 100");
        }
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sortParam.split(",");
        if (parts.length != 2) {
            throw new InvalidUserRequestException("Invalid sort format. Use field,direction");
        }

        String field = parts[0].trim();
        String directionRaw = parts[1].trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new InvalidUserRequestException("Invalid sort field: " + field);
        }

        Sort.Direction direction;
        if ("asc".equals(directionRaw)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equals(directionRaw)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new InvalidUserRequestException("Invalid sort direction: " + directionRaw);
        }

        return Sort.by(direction, field);
    }

        private Specification<User> buildSpecification(String name, String email, String role, boolean activeOnly) {
        UserRole parsedRole = parseRole(role);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Predicate activePredicate = activeOnly
                ? cb.isTrue(root.get("isActive"))
                : cb.isFalse(root.get("isActive"));
            predicates.add(activePredicate);

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + name.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("email")),
                        "%" + email.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (parsedRole != null) {
                predicates.add(cb.equal(root.get("role"), parsedRole));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidUserRequestException("Invalid role filter: " + role);
        }
    }

    private UserSummaryResponseData toSummaryResponse(User user) {
        return UserSummaryResponseData.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .mandorCertificationNumber(user.getMandorCertificationNumber())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
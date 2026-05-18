package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.UpdateMyProfileRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.DeletedUserResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserSummaryResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UpdatedMyProfileResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.DuplicateUserException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.mapper.UserDetailAssembler;
import id.ac.ui.cs.advprog.auth.mapper.UserResponseMapper;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.auth.validation.UserQueryValidator;
import java.time.Instant;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryValidator userQueryValidator;
    private final UserResponseMapper userResponseMapper;
    private final UserDetailAssembler userDetailAssembler;

    @Override
    @Transactional(readOnly = true)
    public UserPageResponseData getUsers(int page, int size, String sort, String name, String email, String role) {
        userQueryValidator.validatePageParams(page, size);

        Pageable pageable = PageRequest.of(page, size, userQueryValidator.parseSort(sort));
        Specification<User> specification = buildSpecification(name, email, role, true);

        Page<User> usersPage = userRepository.findAll(specification, pageable);

        List<UserSummaryResponseData> content = usersPage.getContent().stream()
            .map(userResponseMapper::toSummaryResponse)
            .toList();

        return userResponseMapper.toPageResponse(usersPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPageResponseData getDeletedUsers(int page, int size, String sort, String name, String email, String role) {
        userQueryValidator.validatePageParams(page, size);

        Pageable pageable = PageRequest.of(page, size, userQueryValidator.parseSort(sort));
        Specification<User> specification = buildSpecification(name, email, role, false);

        Page<User> usersPage = userRepository.findAll(specification, pageable);

        List<UserSummaryResponseData> content = usersPage.getContent().stream()
                .map(userResponseMapper::toSummaryResponse)
                .toList();

        return userResponseMapper.toPageResponse(usersPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponseData> getAllUsersForMandor() {
        return userRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(userResponseMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponseData getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return userDetailAssembler.toDetailResponse(user);
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

        return userResponseMapper.toDeletedUserResponse(saved, deletedAt);
    }

    @Override
    @Transactional
    public UpdatedMyProfileResponseData updateMyProfile(UUID userId, UpdateMyProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean hasUsernameUpdate = request.getUsername() != null && !request.getUsername().isBlank();
        boolean hasNameUpdate = request.getName() != null && !request.getName().isBlank();
        boolean hasPasswordUpdate = request.getPassword() != null && !request.getPassword().isBlank();

        if (!hasUsernameUpdate && !hasNameUpdate && !hasPasswordUpdate) {
            throw new InvalidUserRequestException("At least one of username, name or password must be provided");
        }

        if (hasUsernameUpdate) {
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
                throw new DuplicateUserException("Username");
            }
            user.setUsername(request.getUsername());
        }

        if (hasNameUpdate) {
            user.setName(request.getName());
        }

        if (hasPasswordUpdate) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);

        return userResponseMapper.toUpdatedProfileResponse(saved);
    }

    private Specification<User> buildSpecification(String name, String email, String role, boolean activeOnly) {
        UserRole parsedRole = userQueryValidator.parseRole(role);

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

}

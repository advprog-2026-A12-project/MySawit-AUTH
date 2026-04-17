package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.request.management.UpdateMyProfileRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.DeletedUserResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserDetailResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UpdatedMyProfileResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.UserPageResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import id.ac.ui.cs.advprog.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

        @Mock
        private RefreshTokenRepository refreshTokenRepository;

        @Mock
        private BuruhMandorAssignmentRepository buruhMandorAssignmentRepository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
                userService = new UserServiceImpl(
                                userRepository,
                                refreshTokenRepository,
                                buruhMandorAssignmentRepository,
                                passwordEncoder
                );
    }

    @Test
    void getUsersReturnsMappedPage() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        UserPageResponseData result = userService.getUsers(0, 20, "createdAt,desc", null, null, null);

        assertEquals(1, result.getContent().size());
        assertEquals("ahmad@example.com", result.getContent().getFirst().getEmail());
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals("createdAt: DESC", pageableCaptor.getValue().getSort().toString());
    }

        @Test
        void getDeletedUsersReturnsMappedPage() {
                User user = User.builder()
                                .id(UUID.randomUUID())
                                .username("ahmad-buruh-a1b2")
                                .email("ahmad@example.com")
                                .name("Ahmad Buruh")
                                .role(UserRole.BURUH)
                                .isActive(false)
                                .createdAt(Instant.now())
                                .build();

                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(user)));

                UserPageResponseData result = userService.getDeletedUsers(0, 20, "createdAt,desc", null, null, null);

                assertEquals(1, result.getContent().size());
                assertEquals("ahmad@example.com", result.getContent().getFirst().getEmail());
                assertEquals(false, result.getContent().getFirst().isActive());
                verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
                assertEquals("createdAt: DESC", pageableCaptor.getValue().getSort().toString());
        }

    @Test
    void getUsersUsesProvidedSort() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        userService.getUsers(0, 10, "email,asc", null, null, null);

        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals("email: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getUsersThrowsOnInvalidRole() {
        InvalidUserRequestException ex = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.getUsers(0, 20, "createdAt,desc", null, null, "invalid_role")
        );

        assertEquals("Invalid role filter: invalid_role", ex.getMessage());
    }

    @Test
    void getUsersThrowsOnInvalidPageSize() {
        InvalidUserRequestException ex = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.getUsers(0, 0, "createdAt,desc", null, null, null)
        );

        assertEquals("size must be between 1 and 100", ex.getMessage());
    }

        @Test
        void getUsersThrowsOnPageSizeAboveLimit() {
                InvalidUserRequestException ex = assertThrows(
                                InvalidUserRequestException.class,
                                () -> userService.getUsers(0, 101, "createdAt,desc", null, null, null)
                );

                assertEquals("size must be between 1 and 100", ex.getMessage());
        }

        @Test
        void getUsersThrowsOnNegativePage() {
                InvalidUserRequestException ex = assertThrows(
                                InvalidUserRequestException.class,
                                () -> userService.getUsers(-1, 20, "createdAt,desc", null, null, null)
                );

                assertEquals("page must be greater than or equal to 0", ex.getMessage());
        }

        @Test
        void getUsersThrowsOnInvalidSortFormat() {
                InvalidUserRequestException ex = assertThrows(
                                InvalidUserRequestException.class,
                                () -> userService.getUsers(0, 20, "createdAt", null, null, null)
                );

                assertEquals("Invalid sort format. Use field,direction", ex.getMessage());
        }

        @Test
        void getUsersThrowsOnInvalidSortField() {
                InvalidUserRequestException ex = assertThrows(
                                InvalidUserRequestException.class,
                                () -> userService.getUsers(0, 20, "username,asc", null, null, null)
                );

                assertEquals("Invalid sort field: username", ex.getMessage());
        }

        @Test
        void getUsersThrowsOnInvalidSortDirection() {
                InvalidUserRequestException ex = assertThrows(
                                InvalidUserRequestException.class,
                                () -> userService.getUsers(0, 20, "createdAt,up", null, null, null)
                );

                assertEquals("Invalid sort direction: up", ex.getMessage());
        }

        @Test
        void getUsersUsesDefaultSortWhenBlank() {
                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                userService.getUsers(0, 10, "  ", null, null, null);

                verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
                assertEquals("createdAt: DESC", pageableCaptor.getValue().getSort().toString());
        }

        @Test
        void getUsersUsesDefaultSortWhenNull() {
                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                userService.getUsers(0, 10, null, null, null, null);

                verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
                assertEquals("createdAt: DESC", pageableCaptor.getValue().getSort().toString());
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getUsersBuildSpecificationEvaluatesAllPredicates() {
                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                userService.getUsers(0, 20, "createdAt,desc", "Ahmad", "ahmad@example.com", "BURUH");

                ArgumentCaptor<Specification<User>> specCaptor = ArgumentCaptor.forClass(Specification.class);
                verify(userRepository).findAll(specCaptor.capture(), any(Pageable.class));
                Specification<User> specification = specCaptor.getValue();

                Root<User> root = mock(Root.class);
                CriteriaQuery<?> query = mock(CriteriaQuery.class);
                CriteriaBuilder cb = mock(CriteriaBuilder.class);

                Path namePath = mock(Path.class);
                Path emailPath = mock(Path.class);
                Path rolePath = mock(Path.class);
                Path activePath = mock(Path.class);
                Expression<String> loweredName = mock(Expression.class);
                Expression<String> loweredEmail = mock(Expression.class);

                Predicate namePredicate = mock(Predicate.class);
                Predicate emailPredicate = mock(Predicate.class);
                Predicate rolePredicate = mock(Predicate.class);
                Predicate activePredicate = mock(Predicate.class);
                Predicate combined = mock(Predicate.class);

                when(root.get("name")).thenReturn(namePath);
                when(root.get("email")).thenReturn(emailPath);
                when(root.get("role")).thenReturn(rolePath);
                when(root.get("isActive")).thenReturn(activePath);
                when(cb.lower(namePath)).thenReturn(loweredName);
                when(cb.lower(emailPath)).thenReturn(loweredEmail);
                when(cb.like(loweredName, "%ahmad%")).thenReturn(namePredicate);
                when(cb.like(loweredEmail, "%ahmad@example.com%")).thenReturn(emailPredicate);
                when(cb.equal(rolePath, UserRole.BURUH)).thenReturn(rolePredicate);
                when(cb.isTrue(activePath)).thenReturn(activePredicate);
                when(cb.and(any(Predicate[].class))).thenReturn(combined);

                Predicate result = specification.toPredicate(root, query, cb);

                assertEquals(combined, result);
                verify(cb).like(loweredName, "%ahmad%");
                verify(cb).like(loweredEmail, "%ahmad@example.com%");
                verify(cb).equal(rolePath, UserRole.BURUH);
                verify(cb).isTrue(activePath);
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getUsersBuildSpecificationWithoutOptionalFilters() {
                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                userService.getUsers(0, 20, "createdAt,desc", null, null, "   ");

                ArgumentCaptor<Specification<User>> specCaptor = ArgumentCaptor.forClass(Specification.class);
                verify(userRepository).findAll(specCaptor.capture(), any(Pageable.class));
                Specification<User> specification = specCaptor.getValue();

                Root<User> root = mock(Root.class);
                CriteriaQuery<?> query = mock(CriteriaQuery.class);
                CriteriaBuilder cb = mock(CriteriaBuilder.class);

                Path activePath = mock(Path.class);
                Predicate activePredicate = mock(Predicate.class);
                Predicate combined = mock(Predicate.class);

                when(root.get("isActive")).thenReturn(activePath);
                when(cb.isTrue(activePath)).thenReturn(activePredicate);
                when(cb.and(any(Predicate[].class))).thenReturn(combined);

                Predicate result = specification.toPredicate(root, query, cb);

                assertEquals(combined, result);
                verify(cb).isTrue(activePath);
                verify(cb, never()).like(any(), anyString());
                verify(cb, never()).equal(any(), any());
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getUsersBuildSpecificationSkipsBlankNameAndEmail() {
                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                userService.getUsers(0, 20, "createdAt,desc", "   ", "   ", null);

                ArgumentCaptor<Specification<User>> specCaptor = ArgumentCaptor.forClass(Specification.class);
                verify(userRepository).findAll(specCaptor.capture(), any(Pageable.class));
                Specification<User> specification = specCaptor.getValue();

                Root<User> root = mock(Root.class);
                CriteriaQuery<?> query = mock(CriteriaQuery.class);
                CriteriaBuilder cb = mock(CriteriaBuilder.class);

                Path activePath = mock(Path.class);
                Predicate activePredicate = mock(Predicate.class);
                Predicate combined = mock(Predicate.class);

                when(root.get("isActive")).thenReturn(activePath);
                when(cb.isTrue(activePath)).thenReturn(activePredicate);
                when(cb.and(any(Predicate[].class))).thenReturn(combined);

                Predicate result = specification.toPredicate(root, query, cb);

                assertEquals(combined, result);
                verify(cb).isTrue(activePath);
                verify(cb, never()).like(any(), anyString());
                verify(cb, never()).equal(any(), any());
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getDeletedUsersBuildSpecificationEvaluatesInactivePredicate() {
                when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                userService.getDeletedUsers(0, 20, "createdAt,desc", "Ahmad", "ahmad@example.com", "BURUH");

                ArgumentCaptor<Specification<User>> specCaptor = ArgumentCaptor.forClass(Specification.class);
                verify(userRepository).findAll(specCaptor.capture(), any(Pageable.class));
                Specification<User> specification = specCaptor.getValue();

                Root<User> root = mock(Root.class);
                CriteriaQuery<?> query = mock(CriteriaQuery.class);
                CriteriaBuilder cb = mock(CriteriaBuilder.class);

                Path namePath = mock(Path.class);
                Path emailPath = mock(Path.class);
                Path rolePath = mock(Path.class);
                Path activePath = mock(Path.class);
                Expression<String> loweredName = mock(Expression.class);
                Expression<String> loweredEmail = mock(Expression.class);

                Predicate namePredicate = mock(Predicate.class);
                Predicate emailPredicate = mock(Predicate.class);
                Predicate rolePredicate = mock(Predicate.class);
                Predicate inactivePredicate = mock(Predicate.class);
                Predicate combined = mock(Predicate.class);

                when(root.get("name")).thenReturn(namePath);
                when(root.get("email")).thenReturn(emailPath);
                when(root.get("role")).thenReturn(rolePath);
                when(root.get("isActive")).thenReturn(activePath);
                when(cb.lower(namePath)).thenReturn(loweredName);
                when(cb.lower(emailPath)).thenReturn(loweredEmail);
                when(cb.like(loweredName, "%ahmad%")).thenReturn(namePredicate);
                when(cb.like(loweredEmail, "%ahmad@example.com%")).thenReturn(emailPredicate);
                when(cb.equal(rolePath, UserRole.BURUH)).thenReturn(rolePredicate);
                when(cb.isFalse(activePath)).thenReturn(inactivePredicate);
                when(cb.and(any(Predicate[].class))).thenReturn(combined);

                Predicate result = specification.toPredicate(root, query, cb);

                assertEquals(combined, result);
                verify(cb).like(loweredName, "%ahmad%");
                verify(cb).like(loweredEmail, "%ahmad@example.com%");
                verify(cb).equal(rolePath, UserRole.BURUH);
                verify(cb).isFalse(activePath);
        }

    @Test
    void getUserByIdReturnsDetail() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .oauthProvider("GOOGLE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(buruhMandorAssignmentRepository.findByBuruhIdAndIsActiveTrue(userId)).thenReturn(Optional.empty());

        UserDetailResponseData detail = userService.getUserById(userId);

        assertEquals(userId, detail.getId());
        assertEquals("ahmad@example.com", detail.getEmail());
        assertEquals("BURUH", detail.getRole());
        assertEquals("GOOGLE", detail.getOauthProvider());
        assertEquals(Map.of(), detail.getRoleSpecificData());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserByIdForBuruhIncludesAssignedMandorData() {
        UUID userId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();
        Instant assignedAt = Instant.now();

        User buruh = User.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User mandor = User.builder()
                .id(mandorId)
                .username("budi-mandor-e5f6")
                .email("budi@example.com")
                .name("Budi Mandor")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        BuruhMandorAssignment assignment = BuruhMandorAssignment.builder()
                .buruh(buruh)
                .mandor(mandor)
                .isActive(true)
                .assignedAt(assignedAt)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(buruh));
        when(buruhMandorAssignmentRepository.findByBuruhIdAndIsActiveTrue(userId)).thenReturn(Optional.of(assignment));

        UserDetailResponseData detail = userService.getUserById(userId);
        Map<String, Object> assignedMandor = (Map<String, Object>) detail.getRoleSpecificData().get("assignedMandor");

        assertEquals(mandorId, assignedMandor.get("id"));
        assertEquals("Budi Mandor", assignedMandor.get("name"));
        assertEquals("budi@example.com", assignedMandor.get("email"));
        assertEquals(assignedAt, detail.getRoleSpecificData().get("assignedAt"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserByIdForMandorIncludesAllAssignedBuruhsFromAssignments() {
        UUID mandorId = UUID.randomUUID();
        UUID buruh1Id = UUID.randomUUID();
        UUID buruh2Id = UUID.randomUUID();

        Instant assignedAt1 = Instant.now().minusSeconds(3600);
        Instant assignedAt2 = Instant.now();

        User mandor = User.builder()
                .id(mandorId)
                .username("budi-mandor-e5f6")
                .email("budi@example.com")
                .name("Budi Mandor")
                .role(UserRole.MANDOR)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User buruh1 = User.builder()
                .id(buruh1Id)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User buruh2 = User.builder()
                .id(buruh2Id)
                .username("doni-buruh-c3d4")
                .email("doni@example.com")
                .name("Doni Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        BuruhMandorAssignment assignment1 = BuruhMandorAssignment.builder()
                .buruh(buruh1)
                .mandor(mandor)
                .isActive(true)
                .assignedAt(assignedAt1)
                .build();

        BuruhMandorAssignment assignment2 = BuruhMandorAssignment.builder()
                .buruh(buruh2)
                .mandor(mandor)
                .isActive(true)
                .assignedAt(assignedAt2)
                .build();

        when(userRepository.findById(mandorId)).thenReturn(Optional.of(mandor));
        when(buruhMandorAssignmentRepository.findAllByMandor_IdAndIsActiveTrue(eq(mandorId), any(Sort.class)))
                .thenReturn(List.of(assignment2, assignment1));

        UserDetailResponseData detail = userService.getUserById(mandorId);

        List<Map<String, Object>> assignedBuruhs =
                (List<Map<String, Object>>) detail.getRoleSpecificData().get("assignedBuruhs");

        assertEquals(2, assignedBuruhs.size());
        assertEquals(2, detail.getRoleSpecificData().get("totalAssignedBuruhs"));
        assertEquals(buruh2Id, assignedBuruhs.getFirst().get("id"));
        assertEquals("Doni Buruh", assignedBuruhs.getFirst().get("name"));
        assertEquals("doni@example.com", assignedBuruhs.getFirst().get("email"));
        assertEquals(assignedAt2, assignedBuruhs.getFirst().get("assignedAt"));
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(userId));

        assertEquals("User with id " + userId + " not found", ex.getMessage());
    }

    @Test
    void updateMyProfileUpdatesNameAndPassword() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .passwordHash("old-hash")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                .name("Ahmad Buruh Updated")
                .password("NewSecureP@ss456")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecureP@ss456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatedMyProfileResponseData response = userService.updateMyProfile(userId, request);

        assertEquals(userId, response.getId());
        assertEquals("Ahmad Buruh Updated", response.getName());
        assertEquals("BURUH", response.getRole());
        verify(passwordEncoder).encode(eq("NewSecureP@ss456"));
    }

    @Test
    void updateMyProfileUpdatesOnlyName() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .passwordHash("old-hash")
                .isActive(true)
                .build();

        UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                .name("Name Only")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatedMyProfileResponseData response = userService.updateMyProfile(userId, request);

        assertEquals("Name Only", response.getName());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateMyProfileUpdatesOnlyPassword() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Original Name")
                .role(UserRole.BURUH)
                .passwordHash("old-hash")
                .isActive(true)
                .build();

        UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                .password("NewSecureP@ss456")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecureP@ss456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatedMyProfileResponseData response = userService.updateMyProfile(userId, request);

        assertEquals("Original Name", response.getName());
        verify(passwordEncoder).encode("NewSecureP@ss456");
    }

    @Test
    void updateMyProfileThrowsWhenNoFieldsProvided() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        UpdateMyProfileRequest request = UpdateMyProfileRequest.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class,
                () -> userService.updateMyProfile(userId, request));

        assertEquals("At least one of name or password must be provided", ex.getMessage());
    }

    @Test
    void updateMyProfileThrowsWhenFieldsAreBlank() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("ahmad-buruh-a1b2")
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                .name("   ")
                .password("   ")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class,
                () -> userService.updateMyProfile(userId, request));

        assertEquals("At least one of name or password must be provided", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

        @Test
        void updateMyProfileThrowsWhenUserMissing() {
                UUID userId = UUID.randomUUID();
                UpdateMyProfileRequest request = UpdateMyProfileRequest.builder()
                                .name("Updated")
                                .build();

                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                                () -> userService.updateMyProfile(userId, request));

                assertEquals("User with id " + userId + " not found", ex.getMessage());
        }

    @Test
    void deleteUserSoftDeletesAndRemovesTokens() {
        UUID targetUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User user = User.builder()
                .id(targetUserId)
                .email("ahmad@example.com")
                .name("Ahmad Buruh")
                .isActive(true)
                .build();

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeletedUserResponseData response = userService.deleteUser(targetUserId, adminId);

        assertEquals(targetUserId, response.getId());
        assertEquals("ahmad@example.com", response.getEmail());
        assertEquals("Ahmad Buruh", response.getName());
        verify(refreshTokenRepository).deleteAllByUser(user);
    }

    @Test
    void deleteUserThrowsWhenAdminDeletesSelf() {
        UUID targetUserId = UUID.randomUUID();
        User user = User.builder()
                .id(targetUserId)
                .isActive(true)
                .build();
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));

        UnprocessableEntityException ex = assertThrows(
                UnprocessableEntityException.class,
                () -> userService.deleteUser(targetUserId, targetUserId)
        );

        assertEquals("Admin cannot delete their own account", ex.getMessage());
    }

    @Test
    void deleteUserThrowsWhenTargetMissing() {
        UUID targetUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(targetUserId, adminId)
        );

        assertEquals("User with id " + targetUserId + " not found", ex.getMessage());
    }

    @Test
    void deleteUserThrowsWhenTargetAlreadyInactive() {
        UUID targetUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User user = User.builder()
                .id(targetUserId)
                .isActive(false)
                .build();
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(user));

        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(targetUserId, adminId)
        );

        assertEquals("User with id " + targetUserId + " not found", ex.getMessage());
    }
}
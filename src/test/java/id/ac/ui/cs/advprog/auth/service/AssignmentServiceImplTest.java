package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorUnassignmentResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.AssignmentConflictException;
import id.ac.ui.cs.advprog.auth.exception.InvalidUserRequestException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.mapper.AssignmentResponseMapper;
import id.ac.ui.cs.advprog.auth.mapper.AssignmentSpecificationBuilder;
import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.auth.validation.AssignmentValidator;
import id.ac.ui.cs.advprog.auth.validation.AssignmentQueryValidator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private BuruhMandorAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    private AssignmentServiceImpl assignmentService;

    @BeforeEach
    void setUp() {
                AssignmentQueryValidator assignmentQueryValidator = new AssignmentQueryValidator();
                AssignmentResponseMapper assignmentResponseMapper = new AssignmentResponseMapper();
        AssignmentValidator assignmentPolicy = new AssignmentValidator(userRepository, assignmentRepository);
        AssignmentSpecificationBuilder assignmentSpecificationBuilder = new AssignmentSpecificationBuilder();

                assignmentService = new AssignmentServiceImpl(
                                assignmentRepository,
                                assignmentQueryValidator,
                assignmentPolicy,
                assignmentSpecificationBuilder,
                                assignmentResponseMapper
                );
    }

    @Test
    void getAssignmentsReturnsPaginatedActiveAssignments() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .name("Ahmad Buruh")
                .email("ahmad@example.com")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User mandor = User.builder()
                .id(mandorId)
                .name("Budi Mandor")
                .email("budi@example.com")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        BuruhMandorAssignment assignment = BuruhMandorAssignment.builder()
                .id(assignmentId)
                .buruh(buruh)
                .mandor(mandor)
                .assignedAt(Instant.now())
                .isActive(true)
                .build();

        Page<BuruhMandorAssignment> page = new PageImpl<>(
                java.util.List.of(assignment),
                PageRequest.of(0, 20),
                1
        );

        when(assignmentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        BuruhMandorAssignmentPageResponseData response = assignmentService.getAssignments(
                0,
                20,
                mandorId,
                "ahmad",
                "budi"
        );

        assertEquals(1, response.getContent().size());
        assertEquals("ahmad@example.com", response.getContent().get(0).getBuruh().getEmail());
        assertEquals("budi@example.com", response.getContent().get(0).getMandor().getEmail());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getAssignmentsThrows400ForInvalidSize() {
        InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class,
                () -> assignmentService.getAssignments(0, 0, null, null, null));

        assertEquals("size must be between 1 and 100", ex.getMessage());
    }

        @Test
        void getAssignmentsThrows400ForSizeAboveLimit() {
                InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class,
                                () -> assignmentService.getAssignments(0, 101, null, null, null));

                assertEquals("size must be between 1 and 100", ex.getMessage());
        }

        @Test
        void getAssignmentsThrows400ForNegativePage() {
                InvalidUserRequestException ex = assertThrows(InvalidUserRequestException.class,
                                () -> assignmentService.getAssignments(-1, 20, null, null, null));

                assertEquals("page must be greater than or equal to 0", ex.getMessage());
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getAssignmentsBuildSpecificationEvaluatesAllPredicates() {
                when(assignmentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(new PageImpl<>(java.util.List.of()));

                UUID mandorId = UUID.randomUUID();
                assignmentService.getAssignments(0, 20, mandorId, "ahmad", "budi");

                ArgumentCaptor<Specification<BuruhMandorAssignment>> specCaptor = ArgumentCaptor.forClass(Specification.class);
                verify(assignmentRepository).findAll(specCaptor.capture(), any(org.springframework.data.domain.Pageable.class));
                Specification<BuruhMandorAssignment> specification = specCaptor.getValue();

                Root<BuruhMandorAssignment> root = mock(Root.class);
                CriteriaQuery<?> query = mock(CriteriaQuery.class);
                CriteriaBuilder cb = mock(CriteriaBuilder.class);

                Path isActivePath = mock(Path.class);
                Path mandorPath = mock(Path.class);
                Path mandorIdPath = mock(Path.class);
                Path buruhPath = mock(Path.class);
                Path buruhNamePath = mock(Path.class);
                Path mandorNamePath = mock(Path.class);

                Expression<String> loweredBuruhName = mock(Expression.class);
                Expression<String> loweredMandorName = mock(Expression.class);

                Predicate activePredicate = mock(Predicate.class);
                Predicate mandorPredicate = mock(Predicate.class);
                Predicate buruhNamePredicate = mock(Predicate.class);
                Predicate mandorNamePredicate = mock(Predicate.class);
                Predicate combined = mock(Predicate.class);

                when(root.get("isActive")).thenReturn(isActivePath);
                when(root.get("mandor")).thenReturn(mandorPath);
                when(mandorPath.get("id")).thenReturn(mandorIdPath);
                when(root.get("buruh")).thenReturn(buruhPath);
                when(buruhPath.get("name")).thenReturn(buruhNamePath);
                when(mandorPath.get("name")).thenReturn(mandorNamePath);

                when(cb.isTrue(isActivePath)).thenReturn(activePredicate);
                when(cb.equal(mandorIdPath, mandorId)).thenReturn(mandorPredicate);
                when(cb.lower(buruhNamePath)).thenReturn(loweredBuruhName);
                when(cb.lower(mandorNamePath)).thenReturn(loweredMandorName);
                when(cb.like(loweredBuruhName, "%ahmad%")).thenReturn(buruhNamePredicate);
                when(cb.like(loweredMandorName, "%budi%")).thenReturn(mandorNamePredicate);
                when(cb.and(any(Predicate[].class))).thenReturn(combined);

                Predicate result = specification.toPredicate(root, query, cb);

                assertEquals(combined, result);
                verify(cb).isTrue(isActivePath);
                verify(cb).equal(mandorIdPath, mandorId);
                verify(cb).like(loweredBuruhName, "%ahmad%");
                verify(cb).like(loweredMandorName, "%budi%");
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getAssignmentsBuildSpecificationWithoutOptionalFilters() {
                when(assignmentRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(new PageImpl<>(java.util.List.of()));

                assignmentService.getAssignments(0, 20, null, "   ", "   ");

                ArgumentCaptor<Specification<BuruhMandorAssignment>> specCaptor = ArgumentCaptor.forClass(Specification.class);
                verify(assignmentRepository).findAll(specCaptor.capture(), any(org.springframework.data.domain.Pageable.class));
                Specification<BuruhMandorAssignment> specification = specCaptor.getValue();

                Root<BuruhMandorAssignment> root = mock(Root.class);
                CriteriaQuery<?> query = mock(CriteriaQuery.class);
                CriteriaBuilder cb = mock(CriteriaBuilder.class);

                Path isActivePath = mock(Path.class);
                Predicate activePredicate = mock(Predicate.class);
                Predicate combined = mock(Predicate.class);

                when(root.get("isActive")).thenReturn(isActivePath);
                when(cb.isTrue(isActivePath)).thenReturn(activePredicate);
                when(cb.and(any(Predicate[].class))).thenReturn(combined);

                Predicate result = specification.toPredicate(root, query, cb);

                assertEquals(combined, result);
                verify(cb).isTrue(isActivePath);
                verify(cb, never()).equal(any(), any());
                verify(cb, never()).like(any(), anyString());
        }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getAssignmentsBuildSpecificationWithoutOptionalFiltersWhenNull() {
                when(assignmentRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(new PageImpl<>(java.util.List.of()));

                assignmentService.getAssignments(0, 20, null, null, null);

                ArgumentCaptor<Specification<BuruhMandorAssignment>> specCaptor = ArgumentCaptor.forClass(Specification.class);
                verify(assignmentRepository).findAll(specCaptor.capture(), any(org.springframework.data.domain.Pageable.class));
                Specification<BuruhMandorAssignment> specification = specCaptor.getValue();

                Root<BuruhMandorAssignment> root = mock(Root.class);
                CriteriaQuery<?> query = mock(CriteriaQuery.class);
                CriteriaBuilder cb = mock(CriteriaBuilder.class);

                Path isActivePath = mock(Path.class);
                Predicate activePredicate = mock(Predicate.class);
                Predicate combined = mock(Predicate.class);

                when(root.get("isActive")).thenReturn(isActivePath);
                when(cb.isTrue(isActivePath)).thenReturn(activePredicate);
                when(cb.and(any(Predicate[].class))).thenReturn(combined);

                Predicate result = specification.toPredicate(root, query, cb);

                assertEquals(combined, result);
                verify(cb).isTrue(isActivePath);
                verify(cb, never()).equal(any(), any());
                verify(cb, never()).like(any(), anyString());
        }

    @Test
    void assignBuruhToMandorReturnsResponseData() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .name("Ahmad Buruh")
                .email("ahmad@example.com")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User mandor = User.builder()
                .id(mandorId)
                .name("Budi Mandor")
                .email("budi@example.com")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        BuruhMandorAssignment savedAssignment = BuruhMandorAssignment.builder()
                .id(assignmentId)
                .buruh(buruh)
                .mandor(mandor)
                .assignedAt(Instant.now())
                .isActive(true)
                .build();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(buruh));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(mandor));
        when(assignmentRepository.existsByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(false);
        when(assignmentRepository.save(any(BuruhMandorAssignment.class))).thenReturn(savedAssignment);

        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(buruhId)
                .mandorId(mandorId)
                .build();

        BuruhMandorAssignmentResponseData response = assignmentService.assignBuruhToMandor(request);

        assertEquals(assignmentId, response.getId());
        assertEquals("ahmad@example.com", response.getBuruh().getEmail());
        assertEquals("budi@example.com", response.getMandor().getEmail());
        verify(assignmentRepository).save(any(BuruhMandorAssignment.class));
    }

    @Test
    void assignBuruhToMandorThrows404WhenBuruhMissing() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        when(userRepository.findById(buruhId)).thenReturn(Optional.empty());

        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(buruhId)
                .mandorId(mandorId)
                .build();

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> assignmentService.assignBuruhToMandor(request));
        assertEquals("Buruh not found", ex.getMessage());
    }

    @Test
    void assignBuruhToMandorThrows422WhenRoleInvalid() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .email("wrong@example.com")
                .name("Wrong")
                .role(UserRole.SUPIR_TRUK)
                .isActive(true)
                .build();

        User mandor = User.builder()
                .id(mandorId)
                .email("budi@example.com")
                .name("Budi")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(buruh));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(mandor));

        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(buruhId)
                .mandorId(mandorId)
                .build();

        UnprocessableEntityException ex = assertThrows(UnprocessableEntityException.class,
                () -> assignmentService.assignBuruhToMandor(request));
        assertEquals("Provided buruhId is not a BURUH user", ex.getMessage());
    }

    @Test
    void assignBuruhToMandorThrows422WhenMandorRoleInvalid() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .email("buruh@example.com")
                .name("Buruh")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User invalidMandor = User.builder()
                .id(mandorId)
                .email("supir@example.com")
                .name("Supir")
                .role(UserRole.SUPIR_TRUK)
                .isActive(true)
                .build();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(buruh));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(invalidMandor));

        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(buruhId)
                .mandorId(mandorId)
                .build();

        UnprocessableEntityException ex = assertThrows(UnprocessableEntityException.class,
                () -> assignmentService.assignBuruhToMandor(request));
        assertEquals("Provided mandorId is not a MANDOR user", ex.getMessage());
    }

    @Test
    void assignBuruhToMandorThrows404WhenMandorInactive() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .role(UserRole.BURUH)
                .isActive(true)
                .build();
        User inactiveMandor = User.builder()
                .id(mandorId)
                .role(UserRole.MANDOR)
                .isActive(false)
                .build();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(buruh));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(inactiveMandor));

        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(buruhId)
                .mandorId(mandorId)
                .build();

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> assignmentService.assignBuruhToMandor(request));
        assertEquals("Mandor not found", ex.getMessage());
    }

    @Test
    void assignBuruhToMandorThrows409WhenAlreadyAssigned() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .name("Ahmad Buruh")
                .email("ahmad@example.com")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User mandor = User.builder()
                .id(mandorId)
                .name("Budi Mandor")
                .email("budi@example.com")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        when(userRepository.findById(buruhId)).thenReturn(Optional.of(buruh));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(mandor));
        when(assignmentRepository.existsByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(true);

        AssignBuruhMandorRequest request = AssignBuruhMandorRequest.builder()
                .buruhId(buruhId)
                .mandorId(mandorId)
                .build();

        AssignmentConflictException ex = assertThrows(AssignmentConflictException.class,
                () -> assignmentService.assignBuruhToMandor(request));
        assertEquals("Buruh already has an active assignment", ex.getMessage());
    }

    @Test
    void reassignBuruhToMandorReturnsResponseData() {
        UUID buruhId = UUID.randomUUID();
        UUID oldMandorId = UUID.randomUUID();
        UUID newMandorId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .name("Ahmad Buruh")
                .email("ahmad@example.com")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User oldMandor = User.builder()
                .id(oldMandorId)
                .name("Budi Mandor")
                .email("budi@example.com")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        User newMandor = User.builder()
                .id(newMandorId)
                .name("Dedi Mandor")
                .email("dedi@example.com")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                .id(UUID.randomUUID())
                .buruh(buruh)
                .mandor(oldMandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        BuruhMandorAssignment newAssignment = BuruhMandorAssignment.builder()
                .id(UUID.randomUUID())
                .buruh(buruh)
                .mandor(newMandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));
        when(userRepository.findById(newMandorId)).thenReturn(Optional.of(newMandor));
        when(assignmentRepository.save(any(BuruhMandorAssignment.class))).thenReturn(newAssignment);

        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder()
                .newMandorId(newMandorId)
                .build();

        BuruhMandorReassignmentResponseData response = assignmentService.reassignBuruhToMandor(buruhId, request);

        assertEquals("Ahmad Buruh", response.getBuruh().getName());
        assertEquals("Budi Mandor", response.getPreviousMandor().getName());
        assertEquals("Dedi Mandor", response.getNewMandor().getName());
    }

    @Test
    void reassignBuruhToMandorThrows404WhenNoActiveAssignment() {
        UUID buruhId = UUID.randomUUID();
        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.empty());

        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder()
                .newMandorId(UUID.randomUUID())
                .build();

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> assignmentService.reassignBuruhToMandor(buruhId, request));
        assertEquals("Buruh not found or has no active assignment", ex.getMessage());
    }

    @Test
    void reassignBuruhToMandorThrows422WhenSameMandor() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .name("Ahmad Buruh")
                .email("ahmad@example.com")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User mandor = User.builder()
                .id(mandorId)
                .name("Budi Mandor")
                .email("budi@example.com")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                .id(UUID.randomUUID())
                .buruh(buruh)
                .mandor(mandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));
        when(userRepository.findById(mandorId)).thenReturn(Optional.of(mandor));

        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder()
                .newMandorId(mandorId)
                .build();

        UnprocessableEntityException ex = assertThrows(UnprocessableEntityException.class,
                () -> assignmentService.reassignBuruhToMandor(buruhId, request));
        assertEquals("New mandor must be different from current mandor", ex.getMessage());
    }

    @Test
    void reassignBuruhToMandorThrows422WhenNewMandorRoleInvalid() {
        UUID buruhId = UUID.randomUUID();
        UUID oldMandorId = UUID.randomUUID();
        UUID newMandorId = UUID.randomUUID();

        User buruh = User.builder().id(buruhId).role(UserRole.BURUH).isActive(true).build();
        User oldMandor = User.builder().id(oldMandorId).role(UserRole.MANDOR).isActive(true).build();
        User invalidNewMandor = User.builder().id(newMandorId).role(UserRole.SUPIR_TRUK).isActive(true).build();

        BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                .buruh(buruh)
                .mandor(oldMandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));
        when(userRepository.findById(newMandorId)).thenReturn(Optional.of(invalidNewMandor));

        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder().newMandorId(newMandorId).build();

        UnprocessableEntityException ex = assertThrows(UnprocessableEntityException.class,
                () -> assignmentService.reassignBuruhToMandor(buruhId, request));
        assertEquals("Provided newMandorId is not a MANDOR user", ex.getMessage());
    }

    @Test
    void reassignBuruhToMandorThrows404WhenStoredBuruhInactive() {
        UUID buruhId = UUID.randomUUID();
        UUID newMandorId = UUID.randomUUID();

        User inactiveBuruh = User.builder().id(buruhId).role(UserRole.BURUH).isActive(false).build();
        User oldMandor = User.builder().id(UUID.randomUUID()).role(UserRole.MANDOR).isActive(true).build();

        BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                .buruh(inactiveBuruh)
                .mandor(oldMandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));

        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder().newMandorId(newMandorId).build();

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> assignmentService.reassignBuruhToMandor(buruhId, request));
        assertEquals("Buruh not found or has no active assignment", ex.getMessage());
    }

    @Test
    void reassignBuruhToMandorThrows404WhenStoredBuruhRoleInvalid() {
        UUID buruhId = UUID.randomUUID();
        UUID newMandorId = UUID.randomUUID();

        User invalidBuruh = User.builder().id(buruhId).role(UserRole.ADMIN).isActive(true).build();
        User oldMandor = User.builder().id(UUID.randomUUID()).role(UserRole.MANDOR).isActive(true).build();

        BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                .buruh(invalidBuruh)
                .mandor(oldMandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));

        ReassignBuruhMandorRequest request = ReassignBuruhMandorRequest.builder().newMandorId(newMandorId).build();

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> assignmentService.reassignBuruhToMandor(buruhId, request));
        assertEquals("Buruh not found or has no active assignment", ex.getMessage());
    }

    @Test
    void unassignBuruhFromMandorReturnsResponseData() {
        UUID buruhId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();

        User buruh = User.builder()
                .id(buruhId)
                .name("Ahmad Buruh")
                .email("ahmad@example.com")
                .role(UserRole.BURUH)
                .isActive(true)
                .build();

        User mandor = User.builder()
                .id(mandorId)
                .name("Budi Mandor")
                .email("budi@example.com")
                .role(UserRole.MANDOR)
                .isActive(true)
                .build();

        BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                .id(UUID.randomUUID())
                .buruh(buruh)
                .mandor(mandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));
        when(assignmentRepository.save(any(BuruhMandorAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BuruhMandorUnassignmentResponseData response = assignmentService.unassignBuruhFromMandor(buruhId);

        assertEquals("Ahmad Buruh", response.getBuruh().getName());
        assertEquals("Budi Mandor", response.getPreviousMandor().getName());
    }

    @Test
    void unassignBuruhFromMandorThrows404WhenNoActiveAssignment() {
        UUID buruhId = UUID.randomUUID();
        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> assignmentService.unassignBuruhFromMandor(buruhId));
        assertEquals("Buruh not found or has no active assignment", ex.getMessage());
    }

        @Test
        void unassignBuruhFromMandorThrows404WhenStoredBuruhInactive() {
                UUID buruhId = UUID.randomUUID();

                User inactiveBuruh = User.builder().id(buruhId).role(UserRole.BURUH).isActive(false).build();
                User mandor = User.builder().id(UUID.randomUUID()).role(UserRole.MANDOR).isActive(true).build();

                BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                                .buruh(inactiveBuruh)
                                .mandor(mandor)
                                .isActive(true)
                                .assignedAt(Instant.now())
                                .build();

                when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));

                UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                                () -> assignmentService.unassignBuruhFromMandor(buruhId));
                assertEquals("Buruh not found or has no active assignment", ex.getMessage());
    }

    @Test
    void unassignBuruhFromMandorThrows404WhenStoredBuruhRoleInvalid() {
        UUID buruhId = UUID.randomUUID();

        User invalidBuruh = User.builder().id(buruhId).role(UserRole.ADMIN).isActive(true).build();
        User mandor = User.builder().id(UUID.randomUUID()).role(UserRole.MANDOR).isActive(true).build();

        BuruhMandorAssignment activeAssignment = BuruhMandorAssignment.builder()
                .buruh(invalidBuruh)
                .mandor(mandor)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        when(assignmentRepository.findByBuruhIdAndIsActiveTrue(buruhId)).thenReturn(Optional.of(activeAssignment));

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> assignmentService.unassignBuruhFromMandor(buruhId));
        assertEquals("Buruh not found or has no active assignment", ex.getMessage());
    }
}
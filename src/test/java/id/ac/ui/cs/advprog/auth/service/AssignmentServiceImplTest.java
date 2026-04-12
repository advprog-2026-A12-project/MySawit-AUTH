package id.ac.ui.cs.advprog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.exception.AssignmentConflictException;
import id.ac.ui.cs.advprog.auth.exception.UnprocessableEntityException;
import id.ac.ui.cs.advprog.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.BuruhMandorAssignmentRepository;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private BuruhMandorAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    private AssignmentServiceImpl assignmentService;

    @BeforeEach
    void setUp() {
        assignmentService = new AssignmentServiceImpl(assignmentRepository, userRepository);
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
}
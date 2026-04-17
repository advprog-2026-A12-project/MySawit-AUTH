package id.ac.ui.cs.advprog.auth.repository;

import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BuruhMandorAssignmentRepository
        extends JpaRepository<BuruhMandorAssignment, UUID>, JpaSpecificationExecutor<BuruhMandorAssignment> {
    boolean existsByBuruhIdAndIsActiveTrue(UUID buruhId);
    Optional<BuruhMandorAssignment> findByBuruhIdAndIsActiveTrue(UUID buruhId);
    List<BuruhMandorAssignment> findAllByMandor_IdAndIsActiveTrue(UUID mandorId, Sort sort);
}
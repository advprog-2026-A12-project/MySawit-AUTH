package id.ac.ui.cs.advprog.auth.repository;

import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuruhMandorAssignmentRepository extends JpaRepository<BuruhMandorAssignment, UUID> {
    boolean existsByBuruhIdAndIsActiveTrue(UUID buruhId);
}
package id.ac.ui.cs.advprog.auth.repository;

import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface BuruhMandorAssignmentRepository
        extends JpaRepository<BuruhMandorAssignment, UUID>, JpaSpecificationExecutor<BuruhMandorAssignment> {
    boolean existsByBuruhIdAndIsActiveTrue(UUID buruhId);

    @EntityGraph(attributePaths = {"buruh", "mandor"})
    Page<BuruhMandorAssignment> findAll(Specification<BuruhMandorAssignment> spec, Pageable pageable);

    Optional<BuruhMandorAssignment> findByBuruhIdAndIsActiveTrue(UUID buruhId);

    @EntityGraph(attributePaths = {"buruh", "mandor"})
    List<BuruhMandorAssignment> findAllByMandor_IdAndIsActiveTrue(UUID mandorId, Sort sort);
}

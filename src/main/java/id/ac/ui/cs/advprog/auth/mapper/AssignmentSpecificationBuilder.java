package id.ac.ui.cs.advprog.auth.mapper;

import id.ac.ui.cs.advprog.auth.model.BuruhMandorAssignment;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class AssignmentSpecificationBuilder {

    public Specification<BuruhMandorAssignment> build(UUID mandorId, String buruhName, String mandorName) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isActive")));

            if (mandorId != null) {
                predicates.add(cb.equal(root.get("mandor").get("id"), mandorId));
            }

            if (buruhName != null && !buruhName.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("buruh").get("name")),
                        "%" + buruhName.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (mandorName != null && !mandorName.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("mandor").get("name")),
                        "%" + mandorName.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}

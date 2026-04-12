package id.ac.ui.cs.advprog.auth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class BuruhMandorAssignmentModelTest {

    @Test
    void onCreateSetsAssignedCreatedAndUpdatedAtWhenAssignedAtMissing() {
        BuruhMandorAssignment assignment = new BuruhMandorAssignment();

        assignment.onCreate();

        assertNotNull(assignment.getAssignedAt());
        assertNotNull(assignment.getCreatedAt());
        assertNotNull(assignment.getUpdatedAt());
        assertEquals(assignment.getCreatedAt(), assignment.getUpdatedAt());
    }

    @Test
    void onCreatePreservesExistingAssignedAt() {
        Instant existingAssignedAt = Instant.parse("2026-01-01T00:00:00Z");
        BuruhMandorAssignment assignment = new BuruhMandorAssignment();
        assignment.setAssignedAt(existingAssignedAt);

        assignment.onCreate();

        assertEquals(existingAssignedAt, assignment.getAssignedAt());
        assertNotNull(assignment.getCreatedAt());
        assertNotNull(assignment.getUpdatedAt());
    }

    @Test
    void onUpdateRefreshesUpdatedAt() {
        BuruhMandorAssignment assignment = new BuruhMandorAssignment();
        assignment.onCreate();
        Instant beforeUpdate = assignment.getUpdatedAt();

        assignment.onUpdate();

        assertNotNull(assignment.getUpdatedAt());
        assertTrue(!assignment.getUpdatedAt().isBefore(beforeUpdate));
    }
}

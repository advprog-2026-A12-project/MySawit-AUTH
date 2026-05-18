package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentPageResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorUnassignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.MandorBuruhPageResponseData;
import java.util.UUID;

public interface AssignmentService {

    BuruhMandorAssignmentPageResponseData getAssignments(
            int page,
            int size,
            UUID mandorId,
            String buruhName,
            String mandorName
    );

    BuruhMandorAssignmentResponseData assignBuruhToMandor(AssignBuruhMandorRequest request);

    BuruhMandorReassignmentResponseData reassignBuruhToMandor(UUID buruhId, ReassignBuruhMandorRequest request);

    BuruhMandorUnassignmentResponseData unassignBuruhFromMandor(UUID buruhId);

    MandorBuruhPageResponseData getBuruhsByMandor(UUID mandorId, int page, int size, String name);
}

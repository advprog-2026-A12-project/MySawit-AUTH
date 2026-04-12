package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.request.management.ReassignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorReassignmentResponseData;
import java.util.UUID;

public interface AssignmentService {

    BuruhMandorAssignmentResponseData assignBuruhToMandor(AssignBuruhMandorRequest request);

    BuruhMandorReassignmentResponseData reassignBuruhToMandor(UUID buruhId, ReassignBuruhMandorRequest request);
}
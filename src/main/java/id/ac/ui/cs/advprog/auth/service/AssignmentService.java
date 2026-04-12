package id.ac.ui.cs.advprog.auth.service;

import id.ac.ui.cs.advprog.auth.dto.request.management.AssignBuruhMandorRequest;
import id.ac.ui.cs.advprog.auth.dto.response.management.BuruhMandorAssignmentResponseData;

public interface AssignmentService {

    BuruhMandorAssignmentResponseData assignBuruhToMandor(AssignBuruhMandorRequest request);
}
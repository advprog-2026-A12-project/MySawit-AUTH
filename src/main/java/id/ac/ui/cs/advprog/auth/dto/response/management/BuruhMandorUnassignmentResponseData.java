package id.ac.ui.cs.advprog.auth.dto.response.management;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BuruhMandorUnassignmentResponseData {
    ReassignmentUserSummaryResponseData buruh;
    ReassignmentUserSummaryResponseData previousMandor;
    Instant unassignedAt;
}
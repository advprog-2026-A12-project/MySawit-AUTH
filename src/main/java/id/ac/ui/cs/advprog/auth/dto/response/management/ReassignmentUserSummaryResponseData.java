package id.ac.ui.cs.advprog.auth.dto.response.management;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReassignmentUserSummaryResponseData {
    UUID id;
    String name;
}
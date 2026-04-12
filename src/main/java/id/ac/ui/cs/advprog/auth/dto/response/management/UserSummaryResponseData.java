package id.ac.ui.cs.advprog.auth.dto.response.management;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummaryResponseData {
    UUID id;
    String username;
    String email;
    String name;
    String role;
    String mandorCertificationNumber;
    boolean isActive;
    Instant createdAt;
}
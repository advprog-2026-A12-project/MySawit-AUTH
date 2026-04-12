package id.ac.ui.cs.advprog.auth.dto.response.management;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailResponseData {
    UUID id;
    String username;
    String email;
    String name;
    String role;
    String mandorCertificationNumber;
    boolean isActive;
    String oauthProvider;
    Instant createdAt;
    Instant updatedAt;
    @Builder.Default
    Map<String, Object> roleSpecificData = Map.of();
}
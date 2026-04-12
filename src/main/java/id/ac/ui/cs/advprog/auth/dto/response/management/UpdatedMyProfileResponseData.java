package id.ac.ui.cs.advprog.auth.dto.response.management;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdatedMyProfileResponseData {
    UUID id;
    String username;
    String email;
    String name;
    String role;
    Instant updatedAt;
}
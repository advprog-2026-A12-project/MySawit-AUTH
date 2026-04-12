package id.ac.ui.cs.advprog.auth.dto.response.management;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeletedUserResponseData {
    UUID id;
    String email;
    String name;
    Instant deletedAt;
}

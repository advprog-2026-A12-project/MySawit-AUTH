package id.ac.ui.cs.advprog.auth.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseData {

    private UUID id;
    private String username;
    private String email;
    private String name;
    private String role;
    private Instant createdAt;
}

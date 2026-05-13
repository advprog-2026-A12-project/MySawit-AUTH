package id.ac.ui.cs.advprog.auth.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message = "Google authorization code is required")
    private String authorizationCode;

    private String redirectUri;

    private String role;

    private String mandorCertificationNumber;
}

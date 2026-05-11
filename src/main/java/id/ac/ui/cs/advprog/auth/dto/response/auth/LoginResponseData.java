package id.ac.ui.cs.advprog.auth.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseData {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
}

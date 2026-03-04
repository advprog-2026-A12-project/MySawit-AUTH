package id.ac.ui.cs.advprog.auth.dto.response;

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
    private String refreshToken;
    private String tokenType;
    private int expiresIn;
}

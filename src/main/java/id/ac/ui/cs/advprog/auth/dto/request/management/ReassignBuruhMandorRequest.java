package id.ac.ui.cs.advprog.auth.dto.request.management;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignBuruhMandorRequest {

    @NotNull(message = "new MandorId is required")
    private UUID newMandorId;
}
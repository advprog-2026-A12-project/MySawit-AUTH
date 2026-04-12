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
public class AssignBuruhMandorRequest {

    @NotNull(message = "buruhId is required")
    private UUID buruhId;

    @NotNull(message = "mandorId is required")
    private UUID mandorId;
}
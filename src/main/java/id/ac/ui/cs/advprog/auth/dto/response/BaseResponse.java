package id.ac.ui.cs.advprog.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private String status;
    private String message;
    private T data;
    private List<FieldErrorDto> errors;
    private Instant timestamp;

    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static BaseResponse<Void> success(String message) {
        return BaseResponse.<Void>builder()
                .status("success")
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .build();
    }

    public static BaseResponse<Void> error(String message, List<FieldErrorDto> errors) {
        return BaseResponse.<Void>builder()
                .status("error")
                .message(message)
                .errors(errors)
                .timestamp(Instant.now())
                .build();
    }

    public static BaseResponse<Void> error(String message) {
        return BaseResponse.<Void>builder()
                .status("error")
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}

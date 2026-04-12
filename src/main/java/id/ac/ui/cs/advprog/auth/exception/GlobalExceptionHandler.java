package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all application exceptions that extend BaseException.
     * The HTTP status is carried by the exception itself.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        ErrorResponse body = ErrorResponse.of(ex.getField(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    /**
     * Handles Jakarta Bean Validation errors triggered by @Valid.
     * Returns 400 with per-field error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldErrors().isEmpty()
            ? null
            : ex.getBindingResult().getFieldErrors().getFirst().getField();
        String message = ex.getBindingResult().getFieldErrors().isEmpty()
            ? "Validation failed"
            : ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        ErrorResponse body = ErrorResponse.of(field, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Catch-all for any unexpected exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse body = ErrorResponse.of("general", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

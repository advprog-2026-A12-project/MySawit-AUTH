package id.ac.ui.cs.advprog.auth.exception;

import org.springframework.http.HttpStatus;

/**
 * 502 - Downstream service call failed.
 */
public class ExternalServiceException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.BAD_GATEWAY;

    public ExternalServiceException(String message) {
        super(STATUS, message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(STATUS, message, cause);
    }
}

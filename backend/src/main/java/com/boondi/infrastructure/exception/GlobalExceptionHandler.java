package com.boondi.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BoondiException.class)
    public ResponseEntity<ApiResponse<Void>> handleBoondiException(
            BoondiException ex, HttpServletRequest request) {
        log.warn("BoondiException: {} - {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validation failed for request {}: {}", request.getRequestURI(), fieldErrors);
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.VALIDATION_FAILED.name(),
                "Request validation failed",
                fieldErrors,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(
            UsernameNotFoundException ex, HttpServletRequest request) {
        log.warn("UsernameNotFoundException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.USER_NOT_FOUND.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Thrown by @PreAuthorize (e.g. AdminController's hasRole('ADMIN')) when a non-admin
    // hits an admin-only endpoint. Without this handler it falls through to the generic
    // 500 handler below, which is the wrong status for an authorization failure.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.ACCESS_DENIED.name(),
                "You do not have permission to perform this action",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ---- Request-shape errors (E10-01): all of these previously fell through to the ----
    // ---- generic 500 handler even though they are client errors.                    ----

    /** Malformed/unparseable JSON body. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Unreadable request body at {}: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.VALIDATION_FAILED.name(),
                "Malformed request body",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /** Path/query param of the wrong type — e.g. a non-UUID where a UUID is expected. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for '{}' at {}", ex.getName(), request.getRequestURI());
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.VALIDATION_FAILED.name(),
                "Invalid value for parameter '" + ex.getName() + "'",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter '{}' at {}", ex.getParameterName(), request.getRequestURI());
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.VALIDATION_FAILED.name(),
                "Missing required parameter '" + ex.getParameterName() + "'",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /** Constraint violations on @Validated method params (query params like limit/q). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> violations = new HashMap<>();
        ex.getConstraintViolations().forEach(v ->
                violations.put(String.valueOf(v.getPropertyPath()), v.getMessage()));
        log.warn("Constraint violations at {}: {}", request.getRequestURI(), violations);
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.VALIDATION_FAILED.name(),
                "Request validation failed",
                violations,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method {} not allowed at {}", ex.getMethod(), request.getRequestURI());
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.METHOD_NOT_ALLOWED.name(),
                "HTTP method not allowed for this endpoint",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /** Unknown route (Spring 6.1+ throws instead of returning a default 404 page). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.NOT_FOUND.name(),
                "Resource not found",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Upload too large at {}", request.getRequestURI());
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.FILE_TOO_LARGE.name(),
                "Uploaded file exceeds the maximum allowed size",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.failure(
                ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

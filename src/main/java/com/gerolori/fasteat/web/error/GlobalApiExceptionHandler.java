package com.gerolori.fasteat.web.error;

import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.security.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalApiExceptionHandler {

    private final TraceIdResolver traceIdResolver;

    public GlobalApiExceptionHandler(TraceIdResolver traceIdResolver) {
        this.traceIdResolver = traceIdResolver;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ValidationDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::fieldErrorDetail)
                .toList();

        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, details);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException exception, HttpServletRequest request) {
        List<ValidationDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::fieldErrorDetail)
                .toList();

        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ValidationDetail> details = exception.getConstraintViolations()
                .stream()
                .map(this::constraintViolationDetail)
                .toList();

        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, details);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        String errorCode = exception instanceof JwtAuthenticationException jwtAuthenticationException
                ? jwtAuthenticationException.getErrorCode()
                : "AUTH_INVALID_TOKEN";
        return response(HttpStatus.UNAUTHORIZED, errorCode, messageOrDefault(exception, "Authentication failed"), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.FORBIDDEN, "AUTHZ_INSUFFICIENT_ROLE", messageOrDefault(exception, "Access denied"), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", messageOrDefault(exception, "Resource not found"), request);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleViolationException(
            BusinessRuleViolationException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, exception.getErrorCode(), messageOrDefault(exception, "Business rule violated"), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred", request);
    }

    private ValidationDetail fieldErrorDetail(FieldError fieldError) {
        return new ValidationDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ValidationDetail constraintViolationDetail(ConstraintViolation<?> violation) {
        return new ValidationDetail(violation.getPropertyPath().toString(), violation.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        return response(status, errorCode, message, request, null);
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            Object details
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                errorCode,
                message,
                status.value(),
                request.getRequestURI(),
                Instant.now(),
                traceIdResolver.resolve(request),
                details
        );

        return ResponseEntity.status(status).body(body);
    }

    private String messageOrDefault(Exception exception, String fallbackMessage) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return fallbackMessage;
        }

        return exception.getMessage();
    }

    public record ValidationDetail(String field, String message) {
    }
}

package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized global exception handler for the REST API.
 * <p>
 * Utilizes {@link RestControllerAdvice} to intercept exceptions thrown by controllers 
 * across the application, translating them into standardized, front-end friendly 
 * JSON error responses.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepts and handles Jakarta Bean Validation errors (e.g., negative amount, future dates).
     * <p>
     * Triggered automatically when a controller method parameter annotated with {@code @Valid} 
     * fails validation. It parses the binding result to extract field-specific error messages.
     * </p>
     *
     * @param ex the {@link MethodArgumentNotValidException} thrown by the validation framework
     * @return a {@link ResponseEntity} containing a structured map of field errors and an HTTP 400 (Bad Request) status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        response.put("details", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles violations of the system's business rules, specifically the 100-transaction limit per client.
     *
     * @param ex the custom {@link TransactionLimitExceededException} thrown by the service layer
     * @return a {@link ResponseEntity} containing the business rule violation details and an HTTP 400 (Bad Request) status
     */
    @ExceptionHandler(TransactionLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionLimitExceeded(TransactionLimitExceededException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Business Rule Violation", ex.getMessage());
    }

    /**
     * Fallback exception handler for catching all unhandled or unexpected server-side errors.
     * <p>
     * Prevents the application from returning bare stack traces to the client, 
     * ensuring security and maintaining a consistent API contract.
     * </p>
     *
     * @param ex the generic {@link Exception} thrown during runtime
     * @return a {@link ResponseEntity} containing the generic error details and an HTTP 500 (Internal Server Error) status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    /**
     * Helper method to construct a consistently formatted JSON response map for standard errors.
     *
     * @param status  the HTTP status to be returned
     * @param error   a short descriptor of the error type
     * @param message a detailed message explaining the error context
     * @return a {@link ResponseEntity} containing the formatted error map
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
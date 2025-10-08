package com.adisaputera.savingapp.handler;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.exception.ResourceNotFoundException;
import com.adisaputera.savingapp.exception.BadRequestException;
import com.adisaputera.savingapp.exception.DuplicateResourceException;
import com.adisaputera.savingapp.exception.UnauthorizedException;
import com.adisaputera.savingapp.exception.ForbiddenException;
import com.adisaputera.savingapp.exception.InternalServerErrorException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle Resource Not Found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Handle Bad Request (400)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle Duplicate Resource (409)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Handle Unauthorized (401)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Handle Forbidden (403)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(ForbiddenException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Handle Validation Errors (@Valid annotations)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message("Validation failed")
                .data(errors)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle Constraint Violation (@Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(fieldName, message);
        }

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message("Validation constraint violation")
                .data(errors)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle Missing Request Parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = String.format("Missing required parameter: %s", ex.getParameterName());
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(message)
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle Type Mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(message)
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle JSON Parse Error
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message("Invalid JSON format or malformed request body")
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Handle Method Not Allowed (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("Method %s not allowed. Supported methods: %s", 
                ex.getMethod(), ex.getSupportedHttpMethods());
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(message)
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // Handle No Handler Found (404)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
        
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(message)
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Handle Internal Server Error (500)
    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiResponse<Object>> handleInternalServerError(InternalServerErrorException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message(ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Handle Any Other Exception (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message("An unexpected error occurred: " + ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Handle Runtime Exception (500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .status("error")
                .message("Runtime error occurred: " + ex.getMessage())
                .data(null)
                .metadata(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
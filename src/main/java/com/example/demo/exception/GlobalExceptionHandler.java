package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // ========================================
    // NOT FOUND (404)
    // ========================================
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        log.warn("NotFoundException: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // ========================================
    // VALIDATION ERROR (400)
    // ========================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", errorMessage);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(ValidationException ex) {
        log.warn("ValidationException: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", extractMessage(ex));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ========================================
    // CONFLICT / REGISTRO DUPLICADO (409)
    // ========================================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("DataIntegrityViolationException: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("reason", "USER_ALREADY_EXISTS");
        body.put("message", "Usuário ou e-mail já cadastrado no sistema.");
        
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // ========================================
    // BUSINESS RULE VIOLATION (422)
    // ========================================
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        log.warn("BusinessRuleViolationException: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", "Business Rule Violation");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ========================================
    // AUTH EXCEPTION (Status Dinâmico) ✅ CORRIGIDO
    // ========================================
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        log.warn("AuthException: {} - Reason: {}", ex.getMessage(), ex.getReason());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("error", "Authentication Error");
        body.put("message", ex.getMessage());
        
        if (ex.getReason() != null) {
            body.put("reason", ex.getReason().name());
        }
        
        // ✅ Status HTTP baseado no tipo de erro
        HttpStatus status = getHttpStatusForAuthReason(ex.getReason());
        body.put("status", status.value());
        
        return new ResponseEntity<>(body, status);
    }

    // ========================================
    // INVALID TOKEN (401)
    // ========================================
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("InvalidTokenException: {} - Reason: {}", ex.getMessage(), ex.getReason());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Invalid Token");
        body.put("message", ex.getMessage());
        
        if (ex.getReason() != null) {
            body.put("reason", ex.getReason().name());
        }
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // ========================================
    // GENERIC EXCEPTION (500)
    // ========================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Something went wrong!");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Mapeia ReasonType para HttpStatus apropriado
     */
    private HttpStatus getHttpStatusForAuthReason(AuthException.ReasonType reason) {
        if (reason == null) {
            return HttpStatus.UNAUTHORIZED;
        }
        
        return switch (reason) {
            case USER_ALREADY_EXISTS -> HttpStatus.CONFLICT;           // 409
            case USER_NOT_FOUND -> HttpStatus.UNAUTHORIZED;           // 401
            case PASSWORD_DOES_NOT_MATCH -> HttpStatus.UNAUTHORIZED;  // 401
            case INVALID_TOKEN -> HttpStatus.UNAUTHORIZED;            // 401
            case LOGOUT_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;   // 500
            default -> HttpStatus.BAD_REQUEST;                       // 400
        };
    }
    
    private String extractMessage(Exception ex) {
        return Optional.ofNullable(ex.getMessage()).orElse(ex.getClass().getSimpleName());
    }
}
package com.example.demo.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BusinessRuleViolationException extends RuntimeException {

    public enum ViolationType {
        ALREADY_HAS_TASK,
        INVALID_STATUS_TRANSITION,
        PASSWORD_DOES_NOT_MATCH,
        TASK_ALREADY_COMPLETED,
        INVALID_DATE,
        DUPLICATE_EMAIL,        // ✅ NOVO
        INVALID_PASSWORD,       // ✅ NOVO
        PASSWORD_MISMATCH,      // ✅ NOVO
        WEAK_PASSWORD,          // ✅ NOVO
        UNAUTHORIZED_ACCESS
    }

    private final ViolationType violationType;

    // ✅ Construtor com 1 parâmetro (original)
    @Builder
    public BusinessRuleViolationException(ViolationType violationType) {
        super("Violação de regra de negócio: " + violationType.name().toLowerCase());
        this.violationType = violationType;
    }

    // ✅ Construtor com 2 parâmetros (NOVO! - O QUE O TaskService USA!)
    public BusinessRuleViolationException(ViolationType violationType, String message) {
        super(message);
        this.violationType = violationType;
    }

    // ✅ Construtor com String (NOVO!)
    public BusinessRuleViolationException(String message) {
        super(message);
        this.violationType = null;
    }

    @Override
    public String getMessage() {
        if (violationType != null) {
            return "Violação de regra de negócio: " + violationType.name().toLowerCase();
        }
        return super.getMessage();
    }
}
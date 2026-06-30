package com.example.demo.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException {

    public enum ReasonType {
        INVALID_SIGNATURE,
        EXPIRED_TOKEN,
        REVOKED_TOKEN,       // ✅ NOVO
        TOKEN_NOT_FOUND,     // ✅ NOVO
        BLACKLISTED_TOKEN
    }

    private final ReasonType reason;
    private final String customMessage;  // ✅ NOVO

    @Builder
    public InvalidTokenException(ReasonType reason) {
        super("Token inválido: " + reason.name().toLowerCase());
        this.reason = reason;
        this.customMessage = null;
    }

    // ✅ NOVO CONSTRUTOR: Aceita String personalizada
    public InvalidTokenException(ReasonType reason, String customMessage) {
        super(customMessage);
        this.reason = reason;
        this.customMessage = customMessage;
    }

    // ✅ NOVO CONSTRUTOR: Aceita apenas String (compatibilidade)
    public InvalidTokenException(String message) {
        super(message);
        this.reason = ReasonType.INVALID_SIGNATURE;
        this.customMessage = message;
    }

    @Override
    public String getMessage() {
        return customMessage != null ? customMessage : 
               "Token inválido: " + reason.name().toLowerCase();
    }
}
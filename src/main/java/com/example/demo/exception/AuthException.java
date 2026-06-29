package com.example.demo.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    public enum ReasonType {
        PASSWORD_DOES_NOT_MATCH,
        INVALID_TOKEN,
        EXPIRED_TOKEN,
        USER_NOT_FOUND,        // ✅ NOVO!
        USER_ALREADY_EXISTS    // ✅ NOVO!
    }

    private final ReasonType reason;

    public AuthException(ReasonType reason) {
        super(getDefaultMessage(reason));
        this.reason = reason;
    }

    public AuthException(String message) {
        super(message);
        this.reason = null;
    }

    private static String getDefaultMessage(ReasonType reason) {
        return switch (reason) {
            case PASSWORD_DOES_NOT_MATCH -> "Senha incorreta";
            case INVALID_TOKEN -> "Token inválido";
            case EXPIRED_TOKEN -> "Token expirado";
            case USER_NOT_FOUND -> "Usuário não encontrado";
            case USER_ALREADY_EXISTS -> "Usuário já cadastrado com este e-mail";
        };
    }
}
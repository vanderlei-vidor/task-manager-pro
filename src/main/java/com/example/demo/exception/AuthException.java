package com.example.demo.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    public enum ReasonType {
        PASSWORD_DOES_NOT_MATCH,  // Senha incorreta
        INVALID_TOKEN,            // Token inválido
        EXPIRED_TOKEN,            // Token expirado
        USER_NOT_FOUND,           // Usuário não encontrado
        USER_ALREADY_EXISTS,      // Usuário já existe
        LOGOUT_FAILED,            // Logout falhou
        ACCOUNT_DISABLED,         // Conta desativada
        ACCOUNT_LOCKED            // Conta bloqueada
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

    public AuthException(ReasonType reason, String customMessage) {
        super(customMessage);
        this.reason = reason;
    }

    private static String getDefaultMessage(ReasonType reason) {
        return switch (reason) {
            case PASSWORD_DOES_NOT_MATCH -> "Senha incorreta";
            case INVALID_TOKEN -> "Token inválido";
            case EXPIRED_TOKEN -> "Token expirado. Faça login novamente.";
            case USER_NOT_FOUND -> "Usuário não encontrado";
            case USER_ALREADY_EXISTS -> "Usuário já cadastrado com este e-mail";
            case LOGOUT_FAILED -> "Falha ao realizar logout";
            case ACCOUNT_DISABLED -> "Conta desativada. Contate o administrador.";
            case ACCOUNT_LOCKED -> "Conta bloqueada por excesso de tentativas. Tente novamente em 30 minutos.";
        };
    }
}
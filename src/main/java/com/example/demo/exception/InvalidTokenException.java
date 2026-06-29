package com.example.demo.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException {

    public enum ReasonType {
        INVALID_SIGNATURE,
        EXPIRED_TOKEN,
        BLACKLISTED_TOKEN
    }

    private final ReasonType reason;

    @Builder
    public InvalidTokenException(ReasonType reason) {
        super("Token inválido: " + reason.name().toLowerCase());
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return "Token inválido: " + 
               reason.name().toLowerCase();
    }
}
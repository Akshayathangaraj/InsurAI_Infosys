package com.insurai.backend.exception;

public class ClaimValidationException extends RuntimeException {
    public ClaimValidationException(String message) {
        super(message);
    }
}

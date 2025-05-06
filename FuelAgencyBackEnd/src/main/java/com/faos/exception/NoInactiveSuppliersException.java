package com.faos.exception;

public class NoInactiveSuppliersException extends RuntimeException {
    public NoInactiveSuppliersException(String message) {
        super(message);
    }
}
package com.faos.exception;

public class NoActiveSuppliersException extends RuntimeException {
    public NoActiveSuppliersException(String message) {
        super(message);
    }
}
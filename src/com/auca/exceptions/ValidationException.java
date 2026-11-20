package com.auca.exceptions;

// Base exception for every business / validation problem in TAMS.
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}

package com.adisaputera.savingapp.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException() {
        super("Access denied. Authentication required.");
    }
}

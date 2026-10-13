package com.nexora.business.exception;

public class BusinessAlreadyExistsException extends RuntimeException {

    public BusinessAlreadyExistsException(String message) {
        super(message);
    }
}
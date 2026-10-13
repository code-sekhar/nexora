package com.nexora.tax.exception;

public class TaxProfileAlreadyExistsException extends RuntimeException {

    public TaxProfileAlreadyExistsException(String message) {
        super(message);
    }
}
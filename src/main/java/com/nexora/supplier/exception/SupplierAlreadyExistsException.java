package com.nexora.supplier.exception;

public class SupplierAlreadyExistsException
        extends RuntimeException {

    public SupplierAlreadyExistsException(String message) {
        super(message);
    }
}
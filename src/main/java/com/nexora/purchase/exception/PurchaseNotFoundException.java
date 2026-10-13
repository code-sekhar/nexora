package com.nexora.purchase.exception;

public class PurchaseNotFoundException
        extends RuntimeException {

    public PurchaseNotFoundException(String message) {
        super(message);
    }
}
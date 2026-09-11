package com.factoryos.exception;

public class InvalidPurchaseOrderStateException extends RuntimeException {
    public InvalidPurchaseOrderStateException(String message) {
        super(message);
    }
}


package com.ndifreke.core_banking_api.exception;

public class InvalidAmountException extends IllegalArgumentException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
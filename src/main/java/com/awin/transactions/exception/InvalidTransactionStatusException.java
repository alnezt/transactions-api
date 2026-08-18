package com.awin.transactions.exception;

public class InvalidTransactionStatusException extends RuntimeException {

    public InvalidTransactionStatusException(String value) {
        super("Invalid status '" + value + "': expected APPROVED or DECLINED");
    }
}

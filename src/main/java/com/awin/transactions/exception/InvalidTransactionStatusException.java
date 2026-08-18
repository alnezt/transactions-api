package com.awin.transactions.exception;

public class InvalidTransactionStatusException extends RuntimeException {

    /** @param value the invalid status string that was supplied. */
    public InvalidTransactionStatusException(String value) {
        super("Invalid status '" + value + "': expected APPROVED or DECLINED");
    }
}

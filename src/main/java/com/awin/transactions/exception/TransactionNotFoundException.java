package com.awin.transactions.exception;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {

    /** @param id the id that could not be found. */
    public TransactionNotFoundException(UUID id) {
        super("Transaction not found: " + id);
    }
}

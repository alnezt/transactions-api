package com.awin.transactions.exception;

import java.util.UUID;

import com.awin.transactions.domain.TransactionStatus;

public class TransactionAlreadyReviewedException extends RuntimeException {

    public TransactionAlreadyReviewedException(UUID id, TransactionStatus currentStatus) {
        super("Transaction " + id + " is already " + currentStatus + " and can no longer be reviewed");
    }
}

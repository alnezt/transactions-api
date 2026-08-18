package com.awin.transactions.exception;

import java.util.UUID;

import com.awin.transactions.domain.TransactionStatus;

public class TransactionAlreadyReviewedException extends RuntimeException {

    /**
     * @param id            the transaction that was already reviewed.
     * @param currentStatus its status at the time of the conflicting request.
     */
    public TransactionAlreadyReviewedException(UUID id, TransactionStatus currentStatus) {
        super("Transaction " + id + " is already " + currentStatus + " and can no longer be reviewed");
    }
}

package com.awin.transactions.domain;

import java.util.Locale;
import java.util.Set;

import com.awin.transactions.exception.InvalidTransactionStatusException;

public enum TransactionStatus {
    PENDING,
    APPROVED,
    DECLINED;

    private static final Set<TransactionStatus> REVIEW_DECISIONS = Set.of(APPROVED, DECLINED);

    public static TransactionStatus reviewDecisionOf(String value) {
        try {
            TransactionStatus status = valueOf(value.trim().toUpperCase(Locale.ROOT));
            if (!REVIEW_DECISIONS.contains(status)) {
                throw new InvalidTransactionStatusException(value);
            }
            return status;
        } catch (IllegalArgumentException ex) {
            throw new InvalidTransactionStatusException(value);
        }
    }
}

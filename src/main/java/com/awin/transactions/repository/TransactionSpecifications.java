package com.awin.transactions.repository;

import org.springframework.data.jpa.domain.Specification;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.domain.TransactionStatus;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    /** @return a specification matching {@code status}, or no restriction if it's null. */
    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}

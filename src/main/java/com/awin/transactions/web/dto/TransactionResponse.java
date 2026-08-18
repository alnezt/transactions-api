package com.awin.transactions.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.domain.TransactionStatus;

/**
 * @param id                the transaction id.
 * @param status            the current status.
 * @param saleAmount        the sale amount.
 * @param commissionAmount  the commission amount.
 * @param createdAt         when the transaction was created.
 * @param updatedAt         when the transaction was last updated.
 */
public record TransactionResponse(
        UUID id,
        TransactionStatus status,
        BigDecimal saleAmount,
        BigDecimal commissionAmount,
        Instant createdAt,
        Instant updatedAt) {

    /** @return a response built from the given entity. */
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getStatus(),
                transaction.getSaleAmount(),
                transaction.getCommissionAmount(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt());
    }
}

package com.awin.transactions.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.domain.TransactionStatus;

public record TransactionResponse(
        UUID id,
        TransactionStatus status,
        BigDecimal saleAmount,
        BigDecimal commissionAmount,
        Instant createdAt,
        Instant updatedAt) {

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

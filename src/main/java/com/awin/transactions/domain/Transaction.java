package com.awin.transactions.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.awin.transactions.exception.TransactionAlreadyReviewedException;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "sale_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal saleAmount;

    @Column(name = "commission_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Required by JPA; not for application use. */
    protected Transaction() {
    }

    /** Creates a new transaction in {@code PENDING} status. */
    public Transaction(BigDecimal saleAmount, BigDecimal commissionAmount) {
        this.status = TransactionStatus.PENDING;
        this.saleAmount = saleAmount;
        this.commissionAmount = commissionAmount;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /** Applies an approve/decline decision; fails if the transaction was already reviewed. */
    public void review(TransactionStatus decision) {
        if (status != TransactionStatus.PENDING) {
            throw new TransactionAlreadyReviewedException(id, status);
        }
        status = decision;
        updatedAt = Instant.now();
    }

    /** @return the transaction id. */
    public UUID getId() {
        return id;
    }

    /** @return the current status. */
    public TransactionStatus getStatus() {
        return status;
    }

    /** @return the sale amount. */
    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    /** @return the commission amount. */
    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    /** @return when the transaction was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** @return when the transaction was last updated. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

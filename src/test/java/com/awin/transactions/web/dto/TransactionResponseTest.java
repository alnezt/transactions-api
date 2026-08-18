package com.awin.transactions.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.domain.TransactionStatus;

class TransactionResponseTest {

    /** {@code from} copies every field from the entity into the response, unchanged. */
    @Test
    void fromCopiesAllFieldsFromTheEntity() {
        Transaction transaction = new Transaction(new BigDecimal("100.00"), new BigDecimal("5.00"));
        transaction.review(TransactionStatus.APPROVED);

        TransactionResponse response = TransactionResponse.from(transaction);

        assertThat(response.id()).isEqualTo(transaction.getId());
        assertThat(response.status()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(response.saleAmount()).isEqualByComparingTo("100.00");
        assertThat(response.commissionAmount()).isEqualByComparingTo("5.00");
        assertThat(response.createdAt()).isEqualTo(transaction.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(transaction.getUpdatedAt());
    }

    /** The id isn't assigned until persistence; {@code from} must tolerate a null id. */
    @Test
    void fromToleratesTransactionsWithoutAnAssignedId() {
        Transaction transaction = new Transaction(new BigDecimal("10.00"), new BigDecimal("1.00"));

        TransactionResponse response = TransactionResponse.from(transaction);

        assertThat(response.id()).isNull();
        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);
    }
}

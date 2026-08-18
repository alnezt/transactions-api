package com.awin.transactions.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.domain.TransactionStatus;
import com.awin.transactions.repository.TransactionRepository;
import com.awin.transactions.repository.TransactionSpecifications;

@Repository
public class TransactionDao {

    private final TransactionRepository transactionRepository;

    /** @param transactionRepository the Spring Data repository backing this DAO. */
    public TransactionDao(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Inserts or updates a transaction. */
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    /** @return a page of transactions, optionally filtered by status. */
    public Page<Transaction> findAll(TransactionStatus status, Pageable pageable) {
        return transactionRepository.findAll(TransactionSpecifications.hasStatus(status), pageable);
    }

    /** @return the transaction with this id, if it exists. */
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    /** Locks the row for the duration of the transaction, to serialize concurrent reviews. */
    public Optional<Transaction> findByIdForUpdate(UUID id) {
        return transactionRepository.findByIdForReview(id);
    }
}

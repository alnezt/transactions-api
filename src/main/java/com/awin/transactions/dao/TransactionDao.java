package com.awin.transactions.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.repository.TransactionRepository;

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

    /** Returns all transactions. */
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
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

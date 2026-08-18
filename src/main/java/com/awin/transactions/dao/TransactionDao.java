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

    public TransactionDao(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    public Optional<Transaction> findByIdForUpdate(UUID id) {
        return transactionRepository.findByIdForReview(id);
    }
}

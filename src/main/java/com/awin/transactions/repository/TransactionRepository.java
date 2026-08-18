package com.awin.transactions.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.awin.transactions.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}

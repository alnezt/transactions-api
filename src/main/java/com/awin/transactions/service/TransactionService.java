package com.awin.transactions.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.exception.TransactionNotFoundException;
import com.awin.transactions.repository.TransactionRepository;
import com.awin.transactions.web.dto.CreateTransactionRequest;
import com.awin.transactions.web.dto.TransactionResponse;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        Transaction transaction = new Transaction(request.saleAmount(), request.commissionAmount());
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll().stream()
                .map(TransactionResponse::from)
                .toList();
    }

    public TransactionResponse findById(UUID id) {
        return transactionRepository.findById(id)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }
}

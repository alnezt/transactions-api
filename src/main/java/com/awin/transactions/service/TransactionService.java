package com.awin.transactions.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.awin.transactions.dao.TransactionDao;
import com.awin.transactions.domain.Transaction;
import com.awin.transactions.domain.TransactionStatus;
import com.awin.transactions.exception.TransactionNotFoundException;
import com.awin.transactions.web.dto.CreateTransactionRequest;
import com.awin.transactions.web.dto.ReviewTransactionRequest;
import com.awin.transactions.web.dto.TransactionResponse;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionDao transactionDao;

    /** @param transactionDao the persistence gateway for transactions. */
    public TransactionService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    /** Records a new transaction in {@code PENDING} status. */
    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        Transaction transaction = new Transaction(request.saleAmount(), request.commissionAmount());
        return TransactionResponse.from(transactionDao.save(transaction));
    }

    /** Approves or declines a pending transaction. */
    @Transactional
    public TransactionResponse review(UUID id, ReviewTransactionRequest request) {
        TransactionStatus decision = TransactionStatus.reviewDecisionOf(request.status());
        Transaction transaction = transactionDao.findByIdForUpdate(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        transaction.review(decision);
        return TransactionResponse.from(transaction);
    }

    /** @return all transactions. */
    public List<TransactionResponse> findAll() {
        return transactionDao.findAll().stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /** @return the transaction with this id. */
    public TransactionResponse findById(UUID id) {
        return transactionDao.findById(id)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }
}

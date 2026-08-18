package com.awin.transactions.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.awin.transactions.service.TransactionService;
import com.awin.transactions.web.dto.CreateTransactionRequest;
import com.awin.transactions.web.dto.ReviewTransactionRequest;
import com.awin.transactions.web.dto.TransactionResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /** @param transactionService the service handling transaction use cases. */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** Creates a transaction in {@code PENDING} status. */
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse created = transactionService.create(request);
        return ResponseEntity.created(URI.create("/api/transactions/" + created.id())).body(created);
    }

    /** Lists all transactions. */
    @GetMapping
    public List<TransactionResponse> getAll() {
        return transactionService.findAll();
    }

    /** Fetches a single transaction by id; 404 if it doesn't exist. */
    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable UUID id) {
        return transactionService.findById(id);
    }

    /** Approves or declines a pending transaction; 409 if it was already reviewed. */
    @PatchMapping("/{id}/status")
    public TransactionResponse review(@PathVariable UUID id, @Valid @RequestBody ReviewTransactionRequest request) {
        return transactionService.review(id, request);
    }
}

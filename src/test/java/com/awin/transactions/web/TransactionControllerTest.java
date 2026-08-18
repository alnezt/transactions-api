package com.awin.transactions.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.awin.transactions.domain.TransactionStatus;
import com.awin.transactions.exception.InvalidTransactionStatusException;
import com.awin.transactions.exception.TransactionAlreadyReviewedException;
import com.awin.transactions.exception.TransactionNotFoundException;
import com.awin.transactions.service.TransactionService;
import com.awin.transactions.web.dto.CreateTransactionRequest;
import com.awin.transactions.web.dto.PageResponse;
import com.awin.transactions.web.dto.ReviewTransactionRequest;
import com.awin.transactions.web.dto.TransactionResponse;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private static final UUID TRANSACTION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        given(transactionService.create(any(CreateTransactionRequest.class)))
                .willReturn(response(TransactionStatus.PENDING));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"saleAmount\": 100.00, \"commissionAmount\": 5.00}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/transactions/" + TRANSACTION_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createRejectsNonPositiveSaleAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"saleAmount\": 0, \"commissionAmount\": 5.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.saleAmount").exists());
    }

    @Test
    void createRejectsMissingCommissionAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"saleAmount\": 100.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.commissionAmount").exists());
    }

    @Test
    void listReturnsPagedResponse() throws Exception {
        given(transactionService.findAll(eq(null), any()))
                .willReturn(new PageResponse<>(List.of(response(TransactionStatus.PENDING)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listPassesStatusFilterToService() throws Exception {
        given(transactionService.findAll(eq(TransactionStatus.APPROVED), any()))
                .willReturn(new PageResponse<>(List.of(response(TransactionStatus.APPROVED)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/transactions").param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }

    @Test
    void listRejectsUnknownStatusFilter() throws Exception {
        mockMvc.perform(get("/api/transactions").param("status", "BANANA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturnsTransaction() throws Exception {
        given(transactionService.findById(TRANSACTION_ID)).willReturn(response(TransactionStatus.PENDING));

        mockMvc.perform(get("/api/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRANSACTION_ID.toString()));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        given(transactionService.findById(TRANSACTION_ID))
                .willThrow(new TransactionNotFoundException(TRANSACTION_ID));

        mockMvc.perform(get("/api/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void reviewReturnsUpdatedTransaction() throws Exception {
        given(transactionService.review(eq(TRANSACTION_ID), any(ReviewTransactionRequest.class)))
                .willReturn(response(TransactionStatus.APPROVED));

        mockMvc.perform(patch("/api/transactions/{id}/status", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void reviewReturns409WhenAlreadyReviewed() throws Exception {
        willThrow(new TransactionAlreadyReviewedException(TRANSACTION_ID, TransactionStatus.APPROVED))
                .given(transactionService).review(eq(TRANSACTION_ID), any(ReviewTransactionRequest.class));

        mockMvc.perform(patch("/api/transactions/{id}/status", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"DECLINED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void reviewRejectsDecisionOtherThanApprovedOrDeclined() throws Exception {
        willThrow(new InvalidTransactionStatusException("PENDING"))
                .given(transactionService).review(eq(TRANSACTION_ID), any(ReviewTransactionRequest.class));

        mockMvc.perform(patch("/api/transactions/{id}/status", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"PENDING\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reviewRejectsBlankStatus() throws Exception {
        mockMvc.perform(patch("/api/transactions/{id}/status", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }

    private TransactionResponse response(TransactionStatus status) {
        Instant now = Instant.parse("2026-08-18T10:00:00Z");
        return new TransactionResponse(
                TRANSACTION_ID,
                status,
                new BigDecimal("100.00"),
                new BigDecimal("5.00"),
                now,
                now);
    }
}

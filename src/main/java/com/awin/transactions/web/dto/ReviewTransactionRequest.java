package com.awin.transactions.web.dto;

import jakarta.validation.constraints.NotBlank;

/** @param status the review decision; must be APPROVED or DECLINED. */
public record ReviewTransactionRequest(@NotBlank String status) {
}

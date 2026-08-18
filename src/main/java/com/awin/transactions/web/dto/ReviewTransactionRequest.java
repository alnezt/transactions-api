package com.awin.transactions.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewTransactionRequest(@NotBlank String status) {
}

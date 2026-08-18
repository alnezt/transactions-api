package com.awin.transactions.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateTransactionRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal saleAmount,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal commissionAmount) {
}

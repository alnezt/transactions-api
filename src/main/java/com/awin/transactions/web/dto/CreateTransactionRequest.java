package com.awin.transactions.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * @param saleAmount       the sale amount; must be greater than zero.
 * @param commissionAmount the commission amount; must be greater than zero.
 */
public record CreateTransactionRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal saleAmount,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal commissionAmount) {
}

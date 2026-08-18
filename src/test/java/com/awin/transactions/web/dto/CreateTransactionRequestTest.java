package com.awin.transactions.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateTransactionRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    /** Positive sale and commission amounts pass validation. */
    @Test
    void acceptsPositiveAmounts() {
        var request = new CreateTransactionRequest(new BigDecimal("100.00"), new BigDecimal("5.00"));

        assertThat(validator.validate(request)).isEmpty();
    }

    /** A zero sale amount is rejected: it must be strictly greater than zero. */
    @Test
    void rejectsZeroSaleAmount() {
        var request = new CreateTransactionRequest(BigDecimal.ZERO, new BigDecimal("5.00"));

        assertThat(violationPathsFor(request)).containsExactly("saleAmount");
    }

    /** A negative commission amount is rejected. */
    @Test
    void rejectsNegativeCommissionAmount() {
        var request = new CreateTransactionRequest(new BigDecimal("100.00"), new BigDecimal("-1"));

        assertThat(violationPathsFor(request)).containsExactly("commissionAmount");
    }

    /** Both amounts are required. */
    @Test
    void rejectsNullAmounts() {
        var request = new CreateTransactionRequest(null, null);

        assertThat(violationPathsFor(request)).containsExactlyInAnyOrder("saleAmount", "commissionAmount");
    }

    private Set<String> violationPathsFor(CreateTransactionRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}

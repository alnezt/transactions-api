package com.awin.transactions.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ReviewTransactionRequestTest {

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

    /** Any non-blank status string passes validation; the APPROVED/DECLINED check happens later, in the domain. */
    @Test
    void acceptsANonBlankStatus() {
        assertThat(validator.validate(new ReviewTransactionRequest("APPROVED"))).isEmpty();
    }

    /** A blank status is rejected before it ever reaches the domain layer. */
    @Test
    void rejectsBlankStatus() {
        assertThat(validator.validate(new ReviewTransactionRequest(""))).isNotEmpty();
    }

    /** A null status is rejected the same way. */
    @Test
    void rejectsNullStatus() {
        assertThat(validator.validate(new ReviewTransactionRequest(null))).isNotEmpty();
    }
}

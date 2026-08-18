package com.awin.transactions.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.awin.transactions.domain.Transaction;
import com.awin.transactions.domain.TransactionStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TransactionDao.class)
@Testcontainers
class TransactionDaoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void removeSeedData() {
        entityManager.getEntityManager().createQuery("delete from Transaction").executeUpdate();
    }

    @Test
    void savePersistsTransactionAsPending() {
        Transaction saved = transactionDao.save(newTransaction("100.00", "5.00"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(saved.getSaleAmount()).isEqualByComparingTo("100.00");
        assertThat(saved.getCommissionAmount()).isEqualByComparingTo("5.00");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsSavedTransaction() {
        Transaction saved = transactionDao.save(newTransaction("50.00", "2.50"));

        assertThat(transactionDao.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Transaction::getSaleAmount)
                .isEqualTo(saved.getSaleAmount());
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(transactionDao.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAllReturnsEveryTransactionWhenStatusIsNull() {
        transactionDao.save(newTransaction("10.00", "1.00"));
        transactionDao.save(reviewed(newTransaction("20.00", "2.00"), TransactionStatus.APPROVED));

        Page<Transaction> page = transactionDao.findAll(null, Pageable.unpaged());

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findAllFiltersByStatus() {
        transactionDao.save(newTransaction("10.00", "1.00"));
        transactionDao.save(reviewed(newTransaction("20.00", "2.00"), TransactionStatus.APPROVED));
        transactionDao.save(reviewed(newTransaction("30.00", "3.00"), TransactionStatus.DECLINED));

        Page<Transaction> approved = transactionDao.findAll(TransactionStatus.APPROVED, Pageable.unpaged());

        assertThat(approved.getContent())
                .hasSize(1)
                .allMatch(transaction -> transaction.getStatus() == TransactionStatus.APPROVED);
    }

    @Test
    void findAllAppliesPagination() {
        transactionDao.save(newTransaction("10.00", "1.00"));
        transactionDao.save(newTransaction("20.00", "2.00"));
        transactionDao.save(newTransaction("30.00", "3.00"));

        Page<Transaction> firstPage = transactionDao.findAll(null, PageRequest.of(0, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findByIdForUpdateReturnsTransaction() {
        Transaction saved = transactionDao.save(newTransaction("15.00", "1.50"));

        assertThat(transactionDao.findByIdForUpdate(saved.getId()))
                .isPresent()
                .get()
                .extracting(Transaction::getId)
                .isEqualTo(saved.getId());
    }

    private Transaction newTransaction(String saleAmount, String commissionAmount) {
        return new Transaction(new BigDecimal(saleAmount), new BigDecimal(commissionAmount));
    }

    private Transaction reviewed(Transaction transaction, TransactionStatus decision) {
        transaction.review(decision);
        return transaction;
    }
}

package com.example.demo.service;

import com.example.demo.exception.TransactionLimitExceededException;
import com.example.demo.model.Transaction;
import com.example.demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link TransactionService} business logic layer.
 * <p>
 * Utilizes JUnit 5 and Mockito (via {@link MockitoExtension}) to isolate the 
 * service layer from the underlying database infrastructure. This ensures that 
 * core business rules, particularly the maximum transaction limits, are validated 
 * accurately without requiring an active PostgreSQL connection.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    /**
     * Mocked repository to simulate database interactions and responses.
     */
    @Mock
    private TransactionRepository transactionRepository;

    /**
     * The service instance under test, with all mocked dependencies automatically injected.
     */
    @InjectMocks
    private TransactionService transactionService;

    /**
     * A valid baseline transaction utilized as a test fixture across multiple scenarios.
     */
    private Transaction validTransaction;

    /**
     * Prepares the test environment prior to the execution of each individual test.
     * <p>
     * Initializes a standard {@link Transaction} object with valid data to be used 
     * in assertion comparisons and mocked method returns.
     * </p>
     */
    @BeforeEach
    void setUp() {
        validTransaction = new Transaction();
        validTransaction.setAmount(5000);
        validTransaction.setCommerce("Amazon");
        validTransaction.setClientName("John Doe");
        validTransaction.setTransactionDate(LocalDateTime.now());
    }

    /**
     * Verifies that a transaction is successfully persisted when the client has 
     * not yet reached the maximum allowed transaction limit.
     * <p>
     * <strong>Scenario:</strong> The database reports 99 existing transactions for the client.<br>
     * <strong>Expected Outcome:</strong> The service proceeds to call the repository's save method 
     * and returns the persisted entity.
     * </p>
     */
    @Test
    void createTransaction_ShouldSave_WhenClientHasLessThan100Transactions() {
        // Arrange: Simulate that the client currently has 99 transactions
        when(transactionRepository.countByClientName(anyString())).thenReturn(99L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(validTransaction);

        // Act
        Transaction result = transactionService.createTransaction(validTransaction);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getClientName());
        verify(transactionRepository, times(1)).save(validTransaction);
    }

    /**
     * Verifies that the service enforces the strict business rule preventing a client 
     * from exceeding 100 total transactions.
     * <p>
     * <strong>Scenario:</strong> The database reports 100 existing transactions for the client.<br>
     * <strong>Expected Outcome:</strong> A {@link TransactionLimitExceededException} is thrown, 
     * and the repository's save method is strictly never invoked, protecting database integrity.
     * </p>
     */
    @Test
    void createTransaction_ShouldThrowException_WhenClientHas100OrMoreTransactions() {
        // Arrange: Simulate that the client already has 100 transactions
        when(transactionRepository.countByClientName(anyString())).thenReturn(100L);

        // Act & Assert
        TransactionLimitExceededException exception = assertThrows(
                TransactionLimitExceededException.class,
                () -> transactionService.createTransaction(validTransaction)
        );

        assertTrue(exception.getMessage().contains("has reached the maximum limit"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
package com.example.demo.service;

import com.example.demo.exception.TransactionLimitExceededException;
import com.example.demo.model.Transaction;
import com.example.demo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class encapsulating the business logic for transaction management.
 * <p>
 * This layer acts as the intermediary between the REST controllers and the data access 
 * layer (Repository). It is responsible for enforcing core business rules, managing 
 * database transaction boundaries, and executing CRUD operations safely.
 * </p>
 */
@Service
@RequiredArgsConstructor // Lombok generates a constructor with required arguments (final fields)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    
    /**
     * The maximum number of allowed transactions per client.
     * Enforces the business rule that prevents a single client from exceeding this threshold.
     */
    private static final int MAX_TRANSACTIONS_PER_CLIENT = 100;

    /**
     * Retrieves a comprehensive list of all transactions in the system.
     * <p>
     * Executed within a read-only transaction context to optimize performance 
     * at the database level and prevent unintended data modifications.
     * </p>
     *
     * @return a {@link List} of all {@link Transaction} entities found in the database
     */
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Processes and persists a new transaction after validating business constraints.
     * <p>
     * The method queries the current transaction count for the specified client. 
     * If the count meets or exceeds the defined limit ({@link #MAX_TRANSACTIONS_PER_CLIENT}), 
     * the operation is aborted and an exception is thrown. If the validation passes, 
     * the transaction is saved to the database.
     * </p>
     *
     * @param transaction the {@link Transaction} entity containing the data to be persisted
     * @return the successfully saved {@link Transaction} entity, including its generated ID
     * @throws TransactionLimitExceededException if the client has reached the maximum allowed transactions
     */
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        // 1. Check current transaction count for the client
        long currentCount = transactionRepository.countByClientName(transaction.getClientName());
        
        // 2. Enforce the business rule
        if (currentCount >= MAX_TRANSACTIONS_PER_CLIENT) {
            throw new TransactionLimitExceededException(
                "Client '" + transaction.getClientName() + "' has reached the maximum limit of " + 
                MAX_TRANSACTIONS_PER_CLIENT + " transactions."
            );
        }
        
        // 3. Save if validations pass
        return transactionRepository.save(transaction);
    }

    /**
     * Deletes an existing transaction from the database by its unique identifier.
     *
     * @param id the unique identifier of the transaction to be deleted
     */
    @Transactional
    public void deleteTransaction(Integer id) {
        transactionRepository.deleteById(id);
    }
}
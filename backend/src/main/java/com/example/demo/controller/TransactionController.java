package com.example.demo.controller;

import com.example.demo.model.Transaction;
import com.example.demo.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for handling HTTP requests related to {@link Transaction} operations.
 * <p>
 * This controller exposes endpoints for retrieving, creating, and deleting transactions.
 * Cross-Origin Resource Sharing (CORS) is configured to accept requests from the local 
 * Vite development server.
 * </p>
 */
@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Enables Cross-Origin requests from Vite's local dev server
public class TransactionController {

    /**
     * Service layer responsible for encapsulating transaction business logic.
     */
    private final TransactionService transactionService;

    /**
     * Retrieves a list of all recorded transactions in the system.
     *
     * @return a {@link ResponseEntity} containing a list of {@link Transaction} objects 
     *         and an HTTP 200 (OK) status code.
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /**
     * Processes and persists a newly submitted transaction.
     * <p>
     * The incoming JSON payload is automatically validated against the constraints 
     * defined in the entity (e.g., {@code @Min}, {@code @PastOrPresent}) due to 
     * the {@code @Valid} annotation. If validation fails, a MethodArgumentNotValidException 
     * is thrown before the service layer is invoked.
     * </p>
     *
     * @param transaction the transaction payload mapped from the HTTP request body
     * @return a {@link ResponseEntity} containing the successfully created {@link Transaction} 
     *         and an HTTP 201 (Created) status code.
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    /**
     * Deletes a specific transaction from the database by its unique identifier.
     *
     * @param id the unique identifier of the transaction to be removed
     * @return a {@link ResponseEntity} with an HTTP 204 (No Content) status code, 
     *         indicating the operation was successful and no body content is returned.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Integer id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
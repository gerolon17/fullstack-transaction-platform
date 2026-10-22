package com.example.demo.exception;

/**
 * Custom exception thrown when a client attempts to create a transaction 
 * but has already reached the maximum allowed limit.
 */
public class TransactionLimitExceededException extends RuntimeException {
    
    public TransactionLimitExceededException(String message) {
        super(message);
    }
}
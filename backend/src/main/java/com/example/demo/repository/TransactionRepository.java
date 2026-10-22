package com.example.demo.repository;

import com.example.demo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    /**
     * Counts the number of transactions for a specific client.
     * Used to enforce the 100 transactions limit rule.
     * 
     * @param clientName the name of the client
     * @return total number of transactions associated with the client
     */
    long countByClientName(String clientName);
}
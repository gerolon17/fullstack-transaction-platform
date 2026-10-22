package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity representing a financial transaction made by a client.
 * <p>
 * This class is mapped to the {@code transactions} table in the relational database using JPA.
 * It leverages Lombok for boilerplate code reduction (such as getters, setters, and constructors) 
 * and incorporates Jakarta Validation annotations to enforce strict business data integrity 
 * constraints at the model layer prior to database persistence.
 * </p>
 */
@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    /**
     * The unique, auto-generated identifier for the transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The monetary value of the transaction.
     * <p>
     * Must be explicitly provided and is restricted from being a negative amount 
     * to ensure valid financial records.
     * </p>
     */
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private Integer amount;

    /**
     * The name of the commerce, business, or merchant where the transaction took place.
     */
    @NotBlank(message = "Commerce or business name is required")
    private String commerce;

    /**
     * The name of the client executing the transaction.
     */
    @NotBlank(message = "Client name is required")
    @Column(name = "client_name")
    private String clientName;

    /**
     * The exact chronological date and local time when the transaction occurred.
     * <p>
     * Future timestamps are strictly prohibited by business validation rules.
     * </p>
     */
    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
}
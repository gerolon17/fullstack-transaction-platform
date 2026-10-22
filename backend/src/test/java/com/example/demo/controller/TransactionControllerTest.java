package com.example.demo.controller;

import com.example.demo.model.Transaction;
import com.example.demo.service.RateLimitingService;
import com.example.demo.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the {@link TransactionController} web layer.
 * <p>
 * This class utilizes {@code @WebMvcTest} to perform isolated testing of the REST controller.
 * It focuses exclusively on HTTP routing, request/response serialization, and input validation,
 * mocking out the underlying business logic and database layers.
 * </p>
 */
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    /**
     * Utility for mocking HTTP requests and asserting responses without starting a full HTTP server.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Utility for serializing Java objects to JSON and vice versa.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Mocked service layer to simulate business logic execution and responses.
     */
    @MockBean
    private TransactionService transactionService;

    /**
     * Mocked rate-limiting service.
     * <p>
     * Injected as a mock to prevent the {@code RateLimitInterceptor} from failing 
     * or blocking requests during web layer isolation tests.
     * </p>
     */
    @MockBean
    private RateLimitingService rateLimitingService;

    /**
     * A pre-configured, valid transaction object used as a baseline across multiple test cases.
     */
    private Transaction validTransaction;

    /**
     * Initializes the test environment before the execution of each test method.
     * <p>
     * Sets up a standard, valid {@link Transaction} payload to be used for 
     * serialization and mock expectations.
     * </p>
     */
    @BeforeEach
    void setUp() {
        // Note: In tests, bucket4j interceptor might block if not mocked properly, 
        // but for simple WebMvcTest without the full context, it usually passes 
        // or is bypassed if the WebConfig is not loaded.
        
        validTransaction = new Transaction();
        validTransaction.setAmount(10000);
        validTransaction.setCommerce("Steam");
        validTransaction.setClientName("Jane Doe");
        validTransaction.setTransactionDate(LocalDateTime.now());
    }

    /**
     * Verifies that executing a GET request against the {@code /transaction} endpoint 
     * successfully returns an HTTP 200 (OK) status along with a serialized list of transactions.
     *
     * @throws Exception if an error occurs during request execution or JSON processing
     */
    @Test
    void getAllTransactions_ShouldReturnOk() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(Collections.singletonList(validTransaction));

        mockMvc.perform(get("/transaction")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Jane Doe"));
    }

    /**
     * Verifies that posting a structurally valid JSON payload to the {@code /transaction} 
     * endpoint results in a successful creation and returns an HTTP 201 (Created) status.
     *
     * @throws Exception if an error occurs during request execution or JSON processing
     */
    @Test
    void createTransaction_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(validTransaction);

        mockMvc.perform(post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(10000));
    }

    /**
     * Verifies that the controller's {@code @Valid} constraints successfully intercept 
     * and reject a transaction containing a negative amount.
     * <p>
     * Ensures that an HTTP 400 (Bad Request) is returned along with a structured 
     * JSON error detailing the validation failure.
     * </p>
     *
     * @throws Exception if an error occurs during request execution or JSON processing
     */
    @Test
    void createTransaction_ShouldReturnBadRequest_WhenAmountIsNegative() throws Exception {
        validTransaction.setAmount(-500); // Invalid amount

        mockMvc.perform(post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransaction)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.amount").value("Amount cannot be negative"));
    }
}
package com.example.demo.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing and provisioning rate-limiting buckets per client.
 * <p>
 * This service utilizes the Bucket4j library to enforce API traffic limits. 
 * It maintains an in-memory, thread-safe cache of token buckets mapped to individual 
 * client identifiers, ensuring that each client is independently monitored and restricted 
 * according to the system's rate-limiting policies.
 * </p>
 */
@Service
public class RateLimitingService {

    /**
     * Thread-safe, in-memory cache mapping unique client identifiers to their respective token buckets.
     */
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves the token bucket associated with the specified client identifier.
     * <p>
     * If a bucket does not already exist for the given client, it is automatically 
     * created and securely added to the cache using atomic, thread-safe operations.
     * </p>
     *
     * @param clientId the unique identifier of the client (e.g., IP address or HTTP header)
     * @return the {@link Bucket} instance assigned to the client
     */
    public Bucket resolveBucket(String clientId) {
        return cache.computeIfAbsent(clientId, this::newBucket);
    }

    /**
     * Constructs a new token bucket with the defined rate-limiting business rules.
     * <p>
     * The current policy restricts a client to a maximum of 3 requests per minute. 
     * The bucket capacity is fully replenished at the end of each one-minute interval.
     * </p>
     *
     * @param clientId the unique identifier of the client (passed by computeIfAbsent)
     * @return a newly configured {@link Bucket} instance enforcing the bandwidth limits
     */
    private Bucket newBucket(String clientId) {
        // Business rule: 3 requests per minute per client using the modern Builder API
        Bandwidth limit = Bandwidth.builder()
                .capacity(3)
                .refillIntervally(3, Duration.ofMinutes(1))
                .build();

        return Bucket.builder().addLimit(limit).build();
    }
}
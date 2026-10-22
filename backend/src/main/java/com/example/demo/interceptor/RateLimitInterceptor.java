package com.example.demo.interceptor;

import com.example.demo.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP interceptor responsible for enforcing rate-limiting policies on incoming API requests.
 * <p>
 * This component intercepts requests before they reach the controller layer. It identifies 
 * the client, verifies their available request quota using the {@link RateLimitingService}, 
 * and either allows the request to proceed (attaching remaining quota headers) or rejects 
 * it with an HTTP 429 (Too Many Requests) status.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    /**
     * Service responsible for managing and provisioning Bucket4j token buckets per client.
     */
    private final RateLimitingService rateLimitingService;

    /**
     * Intercepts the execution of a handler (controller) to apply rate limiting.
     * <p>
     * The client is identified primarily by a custom {@code Client-Id} HTTP header, 
     * falling back to the client's remote IP address if the header is absent. If the 
     * client's token bucket has available tokens, the request is allowed and the 
     * {@code X-Rate-Limit-Remaining} header is appended. If the bucket is empty, 
     * the request is aborted, and a JSON error response detailing the retry window 
     * ({@code X-Rate-Limit-Retry-After-Seconds}) is returned.
     * </p>
     *
     * @param request  the current HTTP request
     * @param response the current HTTP response
     * @param handler  the chosen handler to execute, for type and/or instance evaluation
     * @return {@code true} if the execution chain should proceed; {@code false} if the 
     *         rate limit is exceeded and the request should be halted
     * @throws Exception in case of errors during response writing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Identify client by an HTTP header (e.g., Client-Id) or fallback to their IP address
        String clientId = request.getHeader("Client-Id");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddr();
        }

        Bucket tokenBucket = rateLimitingService.resolveBucket(clientId);
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Allowed: attach remaining tokens to the response header
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // Rejected: HTTP 429 Too Many Requests
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{ \"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Try again in " + waitForRefill + " seconds.\" }");
            return false;
        }
    }
}
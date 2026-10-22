package com.example.demo.config;

import com.example.demo.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global web configuration class for the Spring MVC application.
 * <p>
 * This class implements {@link WebMvcConfigurer} to customize the default 
 * Spring MVC configuration, specifically to register application-wide 
 * or path-specific HTTP interceptors.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /**
     * Interceptor responsible for enforcing API rate limits per client.
     */
    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * Registers custom interceptors with the Spring MVC execution chain.
     * <p>
     * The rate-limiting mechanism is explicitly mapped to the {@code /transaction/**} 
     * paths to protect the transaction endpoints from request flooding, ensuring 
     * compliance with the defined API consumption policies.
     * </p>
     *
     * @param registry the {@link InterceptorRegistry} used to register the interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply the rate limit policy exclusively to transaction-related endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/transaction/**");
    }
}
package com.skadu.learn.ratelimiting.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter implements WebFilter {

    private final InMemoryRateLimiter rateLimiter;
    private final Set<String> excludedPaths;

    public RateLimitFilter(InMemoryRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        this.excludedPaths = Set.of("/actuator", "/health", "/favicon.ico");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip rate limiting for excluded paths
        if (excludedPaths.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String clientId = getClientId(exchange.getRequest());
        String key = "rate_limit:" + clientId;

        return rateLimiter.isAllowed(key)
                .flatMap(result -> {
                    ServerHttpResponse response = exchange.getResponse();

                    // Always add rate limit headers
                    response.getHeaders().add("X-RateLimit-Limit", "10");
                    response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(result.tokensRemaining()));
                    response.getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));

                    if (result.allowed()) {
                        return chain.filter(exchange);
                    } else {
                        // Rate limit exceeded
                        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        response.getHeaders().add("Retry-After", "60");

                        String body = """
                        {
                            "error": "Rate limit exceeded",
                            "message": "Too many requests. Please try again later.",
                            "status": 429
                        }
                        """;

                        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
                        response.getHeaders().add("Content-Type", "application/json");
                        return response.writeWith(Mono.just(buffer));
                    }
                });
    }

    private String getClientId(ServerHttpRequest request) {
        // Try to get client ID from header first
        String clientId = request.getHeaders().getFirst("X-Client-ID");
        if (clientId != null && !clientId.isEmpty()) {
            return clientId;
        }

        // Try API key
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey;
        }

        // Fallback to IP address
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }

        return "ip:" + (request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown");
    }
}

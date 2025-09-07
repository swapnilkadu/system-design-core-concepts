package com.skadu.learn.ratelimiting.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements WebFilter {

    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientId = getClientId(exchange.getRequest());
        String key = "rate_limit:" + clientId;

        return rateLimiter.isAllowed(key)
                .flatMap(result -> {
                    if (result.allowed()) {
                        // Add rate limit headers
                        exchange.getResponse().getHeaders()
                                .add("X-RateLimit-Remaining", String.valueOf(result.tokensRemaining()));
                        return chain.filter(exchange);
                    } else {
                        // Rate limit exceeded
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders()
                                .add("X-RateLimit-Remaining", "0");
                        return exchange.getResponse().setComplete();
                    }
                });
    }

    private String getClientId(ServerHttpRequest request) {
        // Try to get client ID from header first
        String clientId = request.getHeaders().getFirst("X-Client-ID");
        if (clientId != null && !clientId.isEmpty()) {
            return clientId;
        }

        // Fallback to IP address
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}
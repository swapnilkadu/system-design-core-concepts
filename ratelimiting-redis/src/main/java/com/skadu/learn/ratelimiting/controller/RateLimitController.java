/*
package com.skadu.learn.ratelimiting.controller;

import com.skadu.learn.ratelimiting.config.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/rate-limit")
public class RateLimitController {

    private final RateLimiter rateLimiter;

    public RateLimitController(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/stats")
    public Mono<Map<String, Object>> getStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("activeBuckets", rateLimiter.bucketCount());
            stats.put("timestamp", Instant.now());
            return stats;
        });
    }
}*/

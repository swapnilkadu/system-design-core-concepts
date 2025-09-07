package com.skadu.learn.ratelimiting.config;

import com.skadu.learn.ratelimiting.model.RateLimitProperties;
import com.skadu.learn.ratelimiting.model.RateLimitResult;
import com.skadu.learn.ratelimiting.model.TokenBucket;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryRateLimiter {

    private final ConcurrentHashMap<String, TokenBucket> buckets;
    private final RateLimitProperties properties;
    private final ScheduledExecutorService cleanupExecutor;

    public InMemoryRateLimiter(RateLimitProperties properties) {
        this.properties = properties;
        this.buckets = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "rate-limiter-cleanup"));

        // Start cleanup task
        startCleanupTask();
    }

    public Mono<RateLimitResult> isAllowed(String key) {
        return Mono.fromCallable(() -> {
            TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(properties.capacity(), properties.refillRate()));
            boolean allowed = bucket.tryConsume();
            int tokensRemaining = bucket.getAvailableTokens();

            return new RateLimitResult(allowed, tokensRemaining);
        });
    }

    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredBuckets,
                properties.cleanupInterval().toMinutes(),
                properties.cleanupInterval().toMinutes(),
                TimeUnit.MINUTES
        );
    }

    private void cleanupExpiredBuckets() {
        long currentTime = System.currentTimeMillis();
        long expirationTime = properties.cleanupInterval().toMillis();

        buckets.entrySet().removeIf(entry -> {
            long lastAccess = entry.getValue().getLastAccessTime();
            return (currentTime - lastAccess) > expirationTime;
        });
    }

    @PreDestroy
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // For monitoring/debugging
    public int getBucketCount() {
        return buckets.size();
    }
}
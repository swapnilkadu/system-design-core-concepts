package com.skadu.learn.ratelimiting.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private int capacity = 10; // max tokens in bucket
    private int refillRate = 5; // tokens per second
    private Duration cleanupInterval = Duration.ofMinutes(5);

    public RateLimitProperties(int capacity, int refillRate, Duration cleanupInterval) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.cleanupInterval = cleanupInterval;
    }

    public int capacity() {
        return capacity;
    }

    public RateLimitProperties setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public int refillRate() {
        return refillRate;
    }

    public RateLimitProperties setRefillRate(int refillRate) {
        this.refillRate = refillRate;
        return this;
    }

    public Duration cleanupInterval() {
        return cleanupInterval;
    }

    public RateLimitProperties setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
        return this;
    }

}

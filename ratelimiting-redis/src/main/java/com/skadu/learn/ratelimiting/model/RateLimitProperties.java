package com.skadu.learn.ratelimiting.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private int capacity = 10; // max tokens in bucket
    private int refillRate = 5; // tokens per second
    private Duration windowDuration = Duration.ofMinutes(5);

    public RateLimitProperties(int capacity, int refillRate, Duration windowDuration) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.windowDuration = windowDuration;
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

    public Duration windowDuration() {
        return windowDuration;
    }

    public RateLimitProperties setWindowDuration(Duration windowDuration) {
        this.windowDuration = windowDuration;
        return this;
    }

}

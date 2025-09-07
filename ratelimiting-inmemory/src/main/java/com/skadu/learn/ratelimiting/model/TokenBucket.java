package com.skadu.learn.ratelimiting.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucket {
    private final int capacity;
    private final int refillRate;
    private final AtomicInteger tokens;
    private final AtomicLong lastRefillTime;

    public TokenBucket(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicInteger(capacity);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    public int getAvailableTokens() {
        refill();
        return tokens.get();
    }

    private void refill() {
        long currentTime = System.currentTimeMillis();
        long lastRefill = lastRefillTime.get();
        long timePassed = currentTime - lastRefill;

        if (timePassed > 1000) { // At least 1 second passed
            int tokensToAdd = (int) (timePassed / 1000 * refillRate);
            if (tokensToAdd > 0) {
                int currentTokens = tokens.get();
                int newTokens = Math.min(capacity, currentTokens + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime.set(currentTime);
            }
        }
    }

    public long getLastAccessTime() {
        return lastRefillTime.get();
    }
}

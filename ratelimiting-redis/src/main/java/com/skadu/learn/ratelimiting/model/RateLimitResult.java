package com.skadu.learn.ratelimiting.model;

public record RateLimitResult(boolean allowed, int tokensRemaining) {}

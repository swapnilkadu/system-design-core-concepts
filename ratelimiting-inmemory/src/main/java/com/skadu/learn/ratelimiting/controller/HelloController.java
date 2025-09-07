package com.skadu.learn.ratelimiting.controller;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/api/test")
    public Mono<Map<String, Object>> test(ServerHttpRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Request successful");
        response.put("timestamp", Instant.now());
        response.put("clientIp", request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown");
        return Mono.just(response);
    }

    @GetMapping("/api/unlimited")
    public Mono<String> unlimited() {
        return Mono.just("This endpoint is not rate limited");
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("OK");
    }
}

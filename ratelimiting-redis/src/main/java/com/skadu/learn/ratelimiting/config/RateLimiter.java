package com.skadu.learn.ratelimiting.config;

import com.skadu.learn.ratelimiting.model.RateLimitProperties;
import com.skadu.learn.ratelimiting.model.RateLimitResult;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RateLimiter {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties properties;
    private final RedisScript<List> script;

    public RateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
                       RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.script = RedisScript.of(getLuaScript(), List.class);
    }

    public Mono<RateLimitResult> isAllowed(String key) {
        List<String> keys = List.of(key);
        List<String> args = List.of(
                String.valueOf(properties.capacity()),
                String.valueOf(properties.refillRate()),
                String.valueOf(System.currentTimeMillis())
        );

        return redisTemplate.execute(script, keys, args)
                .cast(List.class)
                .map(results -> {
                    Long allowed = (Long) results.get(0);
                    Long tokensRemaining = (Long) results.get(1);
                    return new RateLimitResult(allowed == 1, tokensRemaining.intValue());
                })
                .next()
                .switchIfEmpty(Mono.just(new RateLimitResult(false, 0)));
    }

    private String getLuaScript() {
        return """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refill_rate = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            
            local bucket_key = key .. ':bucket'
            local last_refill_key = key .. ':last_refill'
            
            local current_tokens = tonumber(redis.call('GET', bucket_key)) or capacity
            local last_refill = tonumber(redis.call('GET', last_refill_key)) or current_time
            
            local time_passed = math.max(0, current_time - last_refill) / 1000
            local tokens_to_add = math.floor(time_passed * refill_rate)
            local new_tokens = math.min(capacity, current_tokens + tokens_to_add)
            
            if new_tokens >= 1 then
                new_tokens = new_tokens - 1
                redis.call('SET', bucket_key, new_tokens)
                redis.call('SET', last_refill_key, current_time)
                redis.call('EXPIRE', bucket_key, 3600)
                redis.call('EXPIRE', last_refill_key, 3600)
                return {1, new_tokens}
            else
                redis.call('SET', bucket_key, new_tokens)
                redis.call('SET', last_refill_key, current_time)
                redis.call('EXPIRE', bucket_key, 3600)
                redis.call('EXPIRE', last_refill_key, 3600)
                return {0, new_tokens}
            end
            """;
    }
}

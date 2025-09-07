package com.skadu.learn.ratelimiting.config;

import com.skadu.learn.ratelimiting.model.RateLimitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InMemoryRateLimiter rateLimiter(RateLimitProperties properties) {
        return new InMemoryRateLimiter(properties);
    }
}

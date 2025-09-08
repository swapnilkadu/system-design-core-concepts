package com.skadu.learn.ratelimiting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;

@SpringBootApplication(exclude = {
        RedisReactiveAutoConfiguration.class
})

public class LimitRateApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimitRateApplication.class, args);
	}

}

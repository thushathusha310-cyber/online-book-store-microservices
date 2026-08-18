package com.bookstore.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimitConfig {
    @Bean
    KeyResolver clientKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    if (exchange.getRequest().getRemoteAddress() == null) {
                        return "unknown-client";
                    }
                    return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                }));
    }
}


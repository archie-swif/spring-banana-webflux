package com.banana.filter;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public final class TraceWebFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return chain.filter(exchange).subscriberContext(
                Context.of("time", System.currentTimeMillis(), "xxx", "xxx")
        );

    }
}
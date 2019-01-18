package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions
                .route(GET("/test"), serverRequest -> {

                    Flux<ResourceWithStatus> resp = Flux.just("a", "b", "c", "d")
                            .flatMap(s -> validate(s))
                            .map(s -> new ResourceWithStatus("OK", s))
                            .onErrorResume(err -> Mono.just(new ResourceWithStatus("ERR", "C")));

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_STREAM_JSON)
                            .body(resp, ResourceWithStatus.class);
                });
    }

    public Mono<String> validate(String input) {
        if (input.equals("c")) {
            return Mono.error(new RuntimeException("Validation error"));
        }
        return Mono.just(input);
    }
}

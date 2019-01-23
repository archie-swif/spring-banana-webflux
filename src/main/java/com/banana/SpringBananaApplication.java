package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.function.Consumer;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> router() {

        log.info("0");

        return
                RouterFunctions

                        //Return a flux stream that breaks on element #4, as defined in Line constructor
                        .route(GET("/flux"), serverRequest -> {
                            Flux<String> lineFlux = Flux.just("1")
                                    .doOnNext(l -> {
                                        log.info("2");
                                    })
                                    .subscriberContext(c -> c.putAll(Context.of("time", System.currentTimeMillis(), "xxx", "xxx")));
                            ;


                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(lineFlux, String.class);
                        });
    }

    //This one works in .doOnNext()
    private static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {

            if (!signal.isOnNext()) {
                return;
            }

            Optional<String> valueFromContext = signal.getContext().getOrEmpty("xxx");
            if (valueFromContext.isPresent()) {
                try (MDC.MDCCloseable closeable = MDC.putCloseable("xxx", valueFromContext.get())) {
                    logStatement.accept(signal.get());
                }
            } else {
                logStatement.accept(signal.get());
            }
        };
    }

}

package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger("SpringBananaApplication");

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }

    @Autowired
    WebClient client;

    @Bean
    public WebClient webClient() {
        return WebClient.create("http://localhost:8080/");
    }

    @Bean
    public RouterFunction<ServerResponse> router() {

        log.info("HELLO");

        return
                RouterFunctions

                        //Return a flux stream that breaks on element #4, as defined in Line constructor
                        .route(GET("/flux"), serverRequest -> {
                            Flux<String> lineFlux = Flux.just("1")
                                    .delayElements(Duration.ofSeconds(1))
                                    .map(Object::toString)
                                    .map(Line::new)
                                    .flatMap(l ->
                                    {
                                        log.info("1");
                                        return Mono.subscriberContext().map(c -> {
                                            log.info("2");
                                            return l.toString() + " " + c.getOrDefault("time", 0) + "\n";
                                        });
                                    })
                                    .doOnNext(l -> {
                                        log.info("3");
                                    })
                                    .doOnEach(logOnNext(msg -> log.info("FOUR "+msg)))
                                    .log();



                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(lineFlux, String.class);
                        });
    }

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

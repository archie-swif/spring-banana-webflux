package com.banana;

import org.apache.pulsar.client.api.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }


    @Bean
    @Autowired
    public RouterFunction<ServerResponse> router(StockMessenger messenger) {
        return
                RouterFunctions
                        .route(GET("/push"), serverRequest -> {

                            CompletableFuture<MessageId> future = messenger.updateStock();
                            Mono<String> resp = Mono.fromFuture(future)
                                    .map(msgId -> "Published message with Id: " + msgId);

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(resp, String.class);
                        })

                        .andRoute(GET("/listen"), serverRequest -> {

                            messenger.startListener();


                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body("Started!", String.class);
                        });
    }

}

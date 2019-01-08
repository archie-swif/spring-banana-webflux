package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

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
        return
                RouterFunctions

                        //Return a flux stream that breaks on element #4, as defined in Line constructor
                        .route(GET("/test"), serverRequest -> {

                            Mono<String> token = client
                                    .get()
                                    .uri("/getToken")
                                    .retrieve()
                                    .bodyToMono(String.class);

                            Mono<HttpStatus> xxx = token
                                    .flatMap(t ->
                                            client.get()
                                                    .uri("/auth")
                                                    .header("TOKEN", t)
                                                    .exchange()
                                                    .map(ClientResponse::statusCode)
                                    );

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(xxx, HttpStatus.class)
                                    .log();
                        })

                        //Does a WebClient GET request to /flux above
                        .andRoute(GET("/getToken"), serverRequest -> {

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .syncBody("TOOOOOKEN");
                        })

                        //Does a WebClient GET request to /flux above
                        .andRoute(GET("/auth"), serverRequest -> {

                            if (serverRequest.headers().header("TOKEN").isEmpty()) {
                                throw new RuntimeException("NO TOKEN");
                            }

                            return ServerResponse
                                    .status(201)
                                    .build();
                        })

                ;
    }

}

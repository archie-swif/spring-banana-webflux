package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.time.Duration;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }

    @Value("${banana.time:100ms}")
    Duration bananaTime;

    @Bean
    public RouterFunction<ServerResponse> router() {
        return
                RouterFunctions

                        //Return a flux stream that breaks on element #4, as defined in Line constructor
                        .route(GET("/time"), serverRequest ->
                                ServerResponse.ok()
                                        .body(Mono.just("response" + bananaTime).delayElement(bananaTime), String.class));
    }

}



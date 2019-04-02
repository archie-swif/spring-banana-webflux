package com.banana;

import com.banana.data.User;
import com.banana.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }

    @Autowired
    UserRepository userRepository;

    @Bean
    public RouterFunction<ServerResponse> router() {

        return RouterFunctions
                .route(POST("/user"), serverRequest -> {

                    Mono<String> usr = serverRequest.bodyToMono(User.class)
                            .flatMap(u -> userRepository.save(u))
                            .map(u -> u.getLastName())
                            .name("user-chain")
                            .tag("user-id", UUID.randomUUID().toString())
                            .metrics();

                    return ServerResponse.ok().body(usr, String.class);
                })
                .andRoute(GET("/user"), serverRequest -> {
                    return ServerResponse.ok().body(userRepository.findAll(), User.class);
                });
    }


}

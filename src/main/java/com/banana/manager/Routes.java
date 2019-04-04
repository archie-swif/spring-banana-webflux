package com.banana.manager;

import com.banana.data.User;
import com.banana.data.UserRepository;
import com.banana.manager.event.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
@Profile("manager")
public class Routes {

    @Autowired
    UserRepository userRepository;


    @Bean
    public RouterFunction<ServerResponse> router(EventHandler eventHandler) {

        return RouterFunctions

                .route(POST("/user"), serverRequest -> {

                    String id = UUID.randomUUID().toString();

                    Mono<String> bridge = Mono.create(sink -> eventHandler.register(sink, id));

                    Mono<String> sender = serverRequest.bodyToMono(User.class)
                            .doOnNext(u -> u.setId(id))
                            .map(eventHandler::sendTodo)
                            .flatMap(b -> bridge);

                    return ServerResponse.ok().body(sender, String.class);
                })
                .andRoute(GET("/user"), serverRequest -> {
                    return ServerResponse.ok().body(userRepository.findById("67697dc3-39d1-4eef-ba5a-f75dba369a5c"), User.class);
                });
    }

}

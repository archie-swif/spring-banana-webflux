package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }

    @Autowired
    UserRepository userRepo;

    @Autowired
    SecondRepository secondRepo;

    @Bean
    public RouterFunction<ServerResponse> router() {


        return
                RouterFunctions

                        //Return a flux stream that breaks on element #4, as defined in Line constructor
                        .route(GET("/save1"), serverRequest -> {
                            User u = new User(UUID.randomUUID().toString(), "FIRST", "LAST", 10);

                            return ServerResponse.ok().body(userRepo.save(u), User.class);
                        })
                        .andRoute(GET("/save2"), serverRequest -> {
                            Second s = new Second(UUID.randomUUID().toString(), "FIRST", "LAST", 10);
                            return ServerResponse.ok().body(secondRepo.save(s), Second.class);
                        })

                        .andRoute(GET("/users"), serverRequest -> {
                            return ServerResponse.ok()
                                    .body(userRepo.findAll(), User.class);
                        })

                        .andRoute(GET("/second"), serverRequest -> {
                            return ServerResponse.ok()
                                    .body(secondRepo.findById("0bea7640-ea9c-4d42-a63b-fde529cab196"), Second.class);
                        });
    }


}

package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class SpringBananaApplication {
    private static Logger log = LoggerFactory.getLogger(SpringBananaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBananaApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> router() {


        return
                RouterFunctions

                        //Return a flux stream that breaks on element #4, as defined in Line constructor
                        .route(GET("/save1"), serverRequest -> {

                            return ServerResponse.ok().build();
                        });
    }


}

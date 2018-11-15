package com.banana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.time.Duration;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

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
                        .route(POST("/callGetFlux"), serverRequest -> {


                            client.get()
                                    .uri("/flux")
                                    .accept(MediaType.APPLICATION_STREAM_JSON)
                                    .retrieve()
                                    .bodyToFlux(Line.class)
                                    .onErrorContinue((err, obj) -> System.err.println("CONTINUE ON /client" + err))
                                    .subscribe(resp -> System.out.println("GET Response: " + resp));

                            return ServerResponse.ok().build();
                        })


                        .andRoute(GET("/flux"), serverRequest -> {
                            Flux<Line> lineFlux = Flux.just("1", "2", "ERR", "4")
                                    .delayElements(Duration.ofSeconds(1))
                                    .map(Line::new)
//                                    .onErrorContinue((err, ojb) -> System.err.println("Continued on /flux"))
                                    ;


                            return ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(lineFlux, Line.class);
                        })

                        //--------------------------------------------

                        //Sends a Flux body
                        .andRoute(GET("/callPostFlux"), serverRequest -> {


                            Flux<Line> lineFlux = Flux.just("1", "2", "3")
                                    .delayElements(Duration.ofSeconds(2))
                                    .map(Line::new);

                            Flux<Line> resp =
                                    client.post()
                                            .uri("/flux")
                                            .accept(MediaType.APPLICATION_STREAM_JSON)
                                            .contentType(MediaType.APPLICATION_STREAM_JSON)
                                            .body(lineFlux, Line.class)
                                            .retrieve()
                                            .bodyToFlux(Line.class)
                                            .doOnNext(l -> log.info("RECV : " + l))
                                            .log();

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(resp, Line.class);
                        })

                        //Receives a flux body
                        .andRoute(POST("/flux"), serverRequest -> {

                            Flux<Line> response = serverRequest
                                    .bodyToFlux(Line.class)
                                    .map(l -> new Line(l.value + "-PROCESSED"))
                                    .log()
                                    .doOnNext(l -> log.info("POST FLUX Handler " + l));

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(response, Line.class);
                        })

                ;
    }

}

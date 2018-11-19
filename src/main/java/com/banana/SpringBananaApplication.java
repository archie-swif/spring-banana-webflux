package com.banana;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
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
    public WebClient webClient() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> router() {
        return
                RouterFunctions

                        //Return a flux stream that breaks on element #4, as defined in Line constructor
                        .route(GET("/flux"), serverRequest -> {
                            Flux<Line> lineFlux = Flux.just("1", "2", "3", "4", "5")
                                    .delayElements(Duration.ofSeconds(1))
                                    .map(Object::toString)
                                    .map(Line::new)
//                                    .onErrorContinue((err, obj) -> log.error("Error on parsing JSON {}", obj, err))
                                    .log();


                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(lineFlux, Line.class);
                        })

                        //Does a WebClient GET request to /flux above
                        .andRoute(GET("/getFlux"), serverRequest -> {
                            Flux<Line> resp = client.get()
                                    .uri("https://localhost:8443/flux")
                                    .accept(MediaType.APPLICATION_STREAM_JSON)
                                    .retrieve()
                                    .bodyToFlux(Line.class)
                                    .doOnNext(l -> log.info("RECV : " + l));

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(resp, Line.class);
                        })


                        //--------------------------------------------

                        //Receives a flux body
                        .andRoute(POST("/flux"), serverRequest -> {

                            Flux<Line> response = serverRequest
                                    .bodyToFlux(Line.class)
                                    .map(l -> new Line(l.value + "-PROCESSED"))
                                    .doOnNext(l -> log.info("PROCESSED : " + l));

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(response, Line.class);
                        })

                        //Sends a Flux body
                        .andRoute(GET("/postFlux"), serverRequest -> {


                            Flux<Line> lineFlux = Flux.just("1", "2", "3")
                                    .delayElements(Duration.ofSeconds(2))
                                    .map(Line::new);

                            Flux<Line> resp = client.post()
                                    .uri("https://localhost:8443/flux")
                                    .accept(MediaType.APPLICATION_STREAM_JSON)
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(lineFlux, Line.class)
                                    .retrieve()
                                    .bodyToFlux(Line.class)
                                    .doOnNext(l -> log.info("RECV : " + l));

                            return ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                                    .body(resp, Line.class);
                        })

                ;
    }

}

package com.banana.worker.event;

import com.banana.data.User;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;


@Profile("worker")
@Component
@EnableBinding({TodoIn.class, WorkCompleteOut.class})
@Log4j2
public class EventHandler {


    Map<String, MonoSink<String>> sinks = new ConcurrentHashMap<>();
    static final MetricRegistry metrics = new MetricRegistry();

    Histogram loopTime = metrics.histogram(name(com.banana.manager.event.EventHandler.class, "loop-time"));
    Histogram kafkaTime = metrics.histogram(name(com.banana.manager.event.EventHandler.class, "kafka-time"));
    Meter tps = metrics.meter("requests");


    @Autowired
    WorkCompleteOut out;

    @StreamListener
    public void inputHandler(@Input(TodoIn.name) Flux<Message<User>> todo) {

        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);

        todo
                .doOnNext(msg -> {
                    Long time = (Long)msg.getHeaders().get("kafka_receivedTimestamp");
                    kafkaTime.update(Duration.between(Instant.ofEpochMilli(time), Instant.now()).toMillis());
                })
                .map(msg -> msg.getPayload())
                .doOnNext(u -> loopTime.update(Duration.between(u.getCreatedTime(), Instant.now()).toMillis()))
                .doOnNext(u -> tps.mark())
                .map(u -> MessageBuilder.withPayload(u).build())
                .map(msg -> out.output().send(msg))
                .subscribe();
    }

// Reactive sender
//    @StreamListener
//    public void inputHandler(@Input(TodoIn.name) Flux<User> todo, @Output(WorkCompleteIn.name) FluxSender reactiveOut) {
//        Flux<User> savedUsers = todo.flatMap(u -> userRepository.save(u));
//        reactiveOut.send(savedUsers);
//    }

}

package com.banana.manager.event;

import com.banana.data.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.MonoSink;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Profile("manager")
@Component
@Log4j2
public class ReactiveKafkaEventHandler {

    @Autowired
    ObjectMapper mapper;

    Map<String, MonoSink<String>> sinks = new ConcurrentHashMap<>();

    Flux<ReceiverRecord<Integer, String>> consumerFlux;
    KafkaSender<Integer, String> sender;

    public ReactiveKafkaEventHandler() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ReceiverOptions<Integer, String> receiverOptions = ReceiverOptions.create(consumerProps);

        ReceiverOptions<Integer, String> options = receiverOptions.subscription(Collections.singleton("work-complete"))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        consumerFlux = KafkaReceiver.create(options).receive();


        Map<String, Object> senderProps = new HashMap<>();
        senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        senderProps.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        senderProps.put(ProducerConfig.ACKS_CONFIG, "all");
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        SenderOptions<Integer, String> senderOptions = SenderOptions.create(senderProps);

        sender = KafkaSender.create(senderOptions);
    }

    @PostConstruct
    public void initMessaging() {

        consumerFlux
                .map(rec -> {
                    rec.receiverOffset().acknowledge();
                    try {
                        return mapper.readValue(rec.value(), User.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new User();
                })
                .doOnNext(u -> log.info(Duration.between(u.getCreatedTime(), Instant.now())))
                .map(User::getId)
                .doOnNext(id -> sinks.remove(id).success(id))
                .subscribe();
    }

    public void sendTodo(User user) {
        sender.send(
                Flux.just(user)
                        .map(u -> {
                            try {
                                return mapper.writeValueAsString(u);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                            return "error";
                        })
                        .map(json -> SenderRecord.create(new ProducerRecord<>("todo", json), 123))
        ).subscribe();
    }


    public void register(MonoSink<String> sink, String id) {
        sinks.put(id, sink);
    }


}

package com.banana.worker.event;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Profile("worker")
@Component
@Log4j2
public class ReactiveKafkaEventHandler {

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

        ReceiverOptions<Integer, String> options = receiverOptions.subscription(Collections.singleton("todo"))
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
                .doOnNext(rec -> {
                    rec.receiverOffset().acknowledge();
                    sendMessage("work-complete", rec.value());
                })
                .subscribe();

    }

    public void sendMessage(String topic, String message) {
        sender.send(
                Flux.just(message)
                        .map(msg -> SenderRecord.create(new ProducerRecord<>(topic, msg), 1))
        ).subscribe();
    }


}

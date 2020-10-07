package com.banana;

import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfiguration {


    public String TOPIC_NAME = "stock-topic";

    @Bean
    public PulsarClient initClient() throws PulsarClientException {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();

        return client;
    }

    @Bean
    @Autowired
    public Producer<String> initProducer(PulsarClient client) throws PulsarClientException {
        Producer<String> producer = client.newProducer(Schema.STRING)
                .topic(TOPIC_NAME)
                .create();

        return producer;
    }

    @Bean
    @Autowired
    public Consumer<String> initConsumer(PulsarClient client) throws PulsarClientException {

        Consumer<String> consumer = client.newConsumer(Schema.STRING)
                .topic(TOPIC_NAME)
                .subscriptionName("stock-subscription")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscribe();

        return consumer;
    }

}

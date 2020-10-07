package com.banana;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.shade.org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class StockMessenger {
    private static Logger log = LoggerFactory.getLogger(StockMessenger.class);
    @Autowired
    Producer<String> producer;
    @Autowired
    Consumer<String> consumer;
    @Autowired
    private PulsarClient client;

    public CompletableFuture<MessageId> updateStock() {
        TypedMessageBuilder<String> message = producer.newMessage()
                .key("TSLA")
                .value("TSLA:" + RandomStringUtils.randomNumeric(4))
                .property("application", "pulsar-app");

        return message.sendAsync();
    }


    public void startListener() {

        Runnable runnable = () -> {
            while (true) {

                Message<String> msg = null;
                try {
                    msg = consumer.receive();

                    System.out.println("Message received: " + msg.getValue());
                    consumer.acknowledge(msg);
                } catch (Exception e) {
                    if (msg != null) {
                        consumer.negativeAcknowledge(msg);
                    }
                }
            }

        };

        new Thread(runnable).start();


    }
}

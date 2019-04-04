package com.banana.manager.event;

import com.banana.data.User;
import com.banana.data.UserRepository;
import com.couchbase.client.core.lang.Tuple;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Profile("manager")
@Component
@EnableBinding({TodoOut.class, WorkCompleteIn.class})
@Log4j2
public class EventHandler {

    Map<String, MonoSink<String>> sinks = new ConcurrentHashMap<>();

    @Autowired
    TodoOut out;

    @Autowired
    UserRepository userRepository;

    public boolean sendTodo(User user) {
        Message<User> msg = MessageBuilder.withPayload(user).build();
        return out.output().send(msg);
    }

    public void register(MonoSink<String> sink, String id) {
        sinks.put(id, sink);

    }

    @StreamListener
    public void inputHandler(@Input(WorkCompleteIn.name) Flux<User> incomingEvent) {
        incomingEvent
                .map(User::getId)
                .flatMap(id -> userRepository.findById(id).map(User::getId))
                .doOnNext(id -> sinks.remove(id).success(id))
                .subscribe();
    }

}

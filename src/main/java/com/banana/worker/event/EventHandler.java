package com.banana.worker.event;

import com.banana.data.User;
import com.banana.data.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Profile("worker")
@Component
@EnableBinding({TodoIn.class, WorkCompleteOut.class})
@Log4j2
public class EventHandler {

    @Autowired
    UserRepository userRepository;

    @Autowired
    WorkCompleteOut out;

    @StreamListener
    public void inputHandler(@Input(TodoIn.name) Flux<User> todo) {
        todo
                .flatMap(u -> userRepository.save(u))
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

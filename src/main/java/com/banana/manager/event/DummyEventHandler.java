package com.banana.manager.event;

import com.banana.data.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;


@Profile("manager")
@Component
@Log4j2
public class DummyEventHandler {

    public boolean sendTodo(User user) {
        return true;
    }

    public void register(MonoSink<String> sink, String id) {
        Mono.delay(Duration.ofMillis(1))
                .doOnNext(l -> sink.success(id))
                .subscribe();
    }

}

//package com.banana.appender;
//
//import ch.qos.logback.classic.spi.ILoggingEvent;
//import ch.qos.logback.core.AppenderBase;
//import reactor.core.publisher.Mono;
//
//public class MapAppender extends AppenderBase<ILoggingEvent> {
//
//
//    @Override
//    protected void append(ILoggingEvent event) {
//
//        Mono.just(event)
//                .flatMap(e -> Mono.subscriberContext().map(ctx -> ctx.getOrDefault("xxx", "NONE")+" - " + event.getFormattedMessage()))
//                .subscribe(e -> System.out.println(e));
//        System.out.println(event.getFormattedMessage());
//    }
//
//
//}
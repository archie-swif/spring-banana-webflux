package com.banana;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
 */
class MdcContextLifter<T> implements CoreSubscriber<T> {

    private static Logger log = LoggerFactory.getLogger("MdcContextLifter");

    CoreSubscriber<T> coreSubscriber;

    public MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
        this.coreSubscriber = coreSubscriber;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
//        log.info("onSubscribe");
        coreSubscriber.onSubscribe(subscription);
    }

    @Override
    public void onNext(T obj) {
//        log.info("onNext");
        copyToMdc(coreSubscriber.currentContext());
        coreSubscriber.onNext(obj);
    }

    @Override
    public void onError(Throwable t) {
        coreSubscriber.onError(t);
    }

    @Override
    public void onComplete() {
//        log.info("onComplete");

        coreSubscriber.onComplete();
    }


    /**
     * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
     * State of the MDC after calling this method should be same as Reactor [Context] state.
     * One thread-local access only.
     */
    private void copyToMdc(Context context) {

        if (!context.isEmpty()) {
            Map<String, String> map = context.stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));

            MDC.setContextMap(map);
            MDC.put("xxx", "aaa");

        } else {
            //MDC.clear();
        }
    }

}
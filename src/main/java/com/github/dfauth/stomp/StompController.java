package com.github.dfauth.stomp;

import akka.http.scaladsl.model.ws.TextMessage;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompEncoder;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.MultiValueMap;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;


public abstract class StompController<T> implements Processor<T, T> {

    private static final Logger logger = LoggerFactory.getLogger(StompController.class);
    private Subscriber<? super T> subscriber;
    private Consumer<T> websocketConsumer;
    private StompDecoder decoder = new StompDecoder();
    private StompEncoder encoder = new StompEncoder();
    private Consumer<org.springframework.messaging.Message<byte[]>> messageConsumer;
    private BiConsumer<StompCommand, Map<String, List<String>>> stompCommandConsumer;
    private MaybeLater<Subscription> subscription = new MaybeLater();

    public void addWebsocketConsumer(Consumer<T> consumer) {
        this.websocketConsumer = consumer;
    }

    public void addMessageConsumer(Consumer<org.springframework.messaging.Message<byte[]>> consumer) {
        this.messageConsumer = consumer;
    }

    public void addStompCommandConsumer(BiConsumer<StompCommand, Map<String, List<String>>> consumer) {
        this.stompCommandConsumer = consumer;
    }

    public void handleStrict(TextMessage.Strict m) {
        logger.info("received strict: "+m);
        decoder.decode(ByteBuffer.wrap(m.text().getBytes())).stream().forEach(m1 -> {
            this.messageConsumer.accept(m1);
        });
    }

    public void handleGeneric(GenericMessage<byte[]> m) {
        logger.info("received generic: "+m);
        StompCommand cmd = m.getHeaders().get("stompCommand", StompCommand.class);
        if(cmd != null) {
            MultiValueMap headers = m.getHeaders().get("nativeHeaders", MultiValueMap.class);
            stompCommandConsumer.accept(cmd, headers);
        }
    }

    public GenericMessage<byte[]> createMessage(StompCommand c) {
        return new GenericMessage(new byte[]{}, Collections.singletonMap("stompCommand", c));
    }

    public MultiValueMap extractHeaders(GenericMessage<byte[]> c) {
        return c.getHeaders().get("nativeHeaders", MultiValueMap.class);
    }

    public List<String> extractHeader(GenericMessage<byte[]> c, String key) {
        MultiValueMap<String, String> nativeHeaders = extractHeaders(c);
        logger.info("nativeHeaders: "+nativeHeaders);
        return nativeHeaders.get(key);
    }

    public Optional<String> extractHeaderExpectingOne(GenericMessage<byte[]> c, String key) {
        List<String> headers = extractHeader(c, key);
        return headers.stream().findFirst();
    }

    public Optional<String> extractBearerToken(GenericMessage<byte[]> c) {
        Optional<String> bearerToken = extractHeaderExpectingOne(c, "Authorization");
        return bearerToken.flatMap(t -> Stream.of(t.split(" ")).reduce(Utils.getLast()));
    }

    public Optional<String> extractBearerToken(Map<String, List<String>> headers) {
        List<String> bearerToken = headers.get("Authorization");
        return bearerToken.stream().flatMap(t -> Stream.of(t.split(" "))).reduce(Utils.getLast());
    }

    public Optional<String> extractTopic(Map<String, List<String>> headers) {
        List<String> bearerToken = headers.get("destination");
        return bearerToken.stream().flatMap(t -> Stream.of(t.split(" "))).reduce(Utils.getLast());
    }

    public void publish(GenericMessage<byte[]> m) {
        byte[] bytes = encoder.encode(m);
        subscriber.onNext(wrap(new String(bytes)));
    }

    protected abstract T wrap(String s);

    // publisher
    @Override
    public void subscribe(Subscriber<? super T> s) {
        this.subscriber = s;
        this.subscription = subscription.accept(v -> {
            this.subscriber.onSubscribe(v);
        });
    }

    // subscriber
    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
        this.subscription = this.subscription.accept(s);
    }

    @Override
    public void onNext(T m) {
        websocketConsumer.accept(m);
    }

    @Override
    public void onError(Throwable t) {
        subscriber.onError(t);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }
}

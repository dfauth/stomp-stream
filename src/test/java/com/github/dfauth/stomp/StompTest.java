package com.github.dfauth.stomp;

import akka.actor.ActorSystem;
import akka.http.scaladsl.Http;
import akka.stream.ActorMaterializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import scala.concurrent.Future;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.fail;

public class StompTest {

    private static final Logger logger = LoggerFactory.getLogger(StompTest.class);

    private int port = 8081;

    private WebSocketClient sockJsClient;

    private WebSocketStompClient stompClient;

    private StompStreamService thing;
    private Future<Http.ServerBinding> f;

    @org.testng.annotations.BeforeTest
    public void setup() {

        ActorSystem system = ActorSystem.create();
        ActorMaterializer materializer = ActorMaterializer.create(system);
        thing = new StompStreamService(system, materializer, "127.0.0.1", 8081);
        f = thing.start();


        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        this.sockJsClient = new StandardWebSocketClient();

        this.stompClient = new WebSocketStompClient(sockJsClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterTest
    public void tearDown() {
        thing.stop(f);
    }

    @Test
    public void getGreeting() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> failure = new AtomicReference<>();

        StompSessionHandler handler = new TestSessionHandler(failure) {

            @Override
            public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/greetings", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return Object.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        logger.info("payload : "+payload);
//                        Greeting greeting = (Greeting) payload;
//                        try {
//                            assertEquals("Hello, Spring!", greeting.getContent());
//                        } catch (Throwable t) {
//                            failure.set(t);
//                        } finally {
//                            session.disconnect();
//                            latch.countDown();
//                        }
                    }
                });
//                try {
//                    session.send("/app/hello", new HelloMessage("Spring"));
//                } catch (Throwable t) {
//                    failure.set(t);
//                    latch.countDown();
//                }
            }
        };


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth("KJGJGLKGJUIJM<<MB FYDYITDIYF:wq:w");
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.addAll(httpHeaders);
        this.stompClient.connect("ws://127.0.0.1:{port}/subscribe", new WebSocketHttpHeaders(), stompHeaders, handler, this.port);

        if (latch.await(30, TimeUnit.SECONDS)) {
            if (failure.get() != null) {
                throw new AssertionError("", failure.get());
            }
        }
        else {
            fail("Greeting not received");
        }

    }

    private class TestSessionHandler extends StompSessionHandlerAdapter {

        private final AtomicReference<Throwable> failure;


        public TestSessionHandler(AtomicReference<Throwable> failure) {
            this.failure = failure;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            this.failure.set(new Exception(headers.toString()));
        }

        @Override
        public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
            this.failure.set(ex);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable ex) {
            this.failure.set(ex);
        }
    }
}

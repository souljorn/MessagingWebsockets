package com.rsocket.consumer;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@SpringBootApplication
public class ConsumerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
        Server server;
        server = new Server();

//        RSocket rSocket = RSocketFactory.connect()
//                .transport(WebsocketClientTransport.create(
//                        HttpClient.from(TcpClient.create()
//                                .host("localhost")
//                                .port(5555)),
//                        "/ws/profiles"
//                ))
//                .start()
//                .doOnNext(x -> LOGGER.info("Server started"))
//                .flux().blockLast();
// Disposable server = RSocketFactory.receive()
//                .acceptor((setupPayload, reactiveSocket) -> Mono.just(new RSocketImpl()))
//                .transport(TcpServerTransport.create("localhost", 5555))
//                .start()
//                .doOnNext(x -> LOGGER.info("Server started"))
//                .subscribe();

//        LOGGER.info(
////                rSocket.requestResponse(DefaultPayload.create("HelloWorld"))
////                        .map(Payload::getDataUtf8)
////                        .block()
////        );

//        LOGGER.info(
//                rSocket.fireAndForget(DefaultPayload.create("HelloWorld")).toString());

//        LOGGER.info(
//                rSocket.requestStream(DefaultPayload.create("HelloWorld")).toString());

    }





    @Bean
    public SocketAcceptor socketAcceptor() {
        return ((setup, sendingSocket) -> Mono.just(new AbstractRSocket() {
            @Override
            public Mono<Void> fireAndForget(Payload payload) {
                LOGGER.info("Handled fnf with payload: [" + payload + "]");
                return Mono.empty();
            }

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                LOGGER.info("Handled requestResponse with payload: [" + payload + "]");
                return Mono.just(DefaultPayload.create("Echo: " + payload.getDataUtf8()));
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
                LOGGER.info("Handled requestStream with payload: [" + payload + "]");
                return Flux
                        .interval(Duration.ofSeconds(1))
                        .map(i -> DefaultPayload.create(
                                "Echo[" + i + "]:" + payload.getDataUtf8()
                        ));
            }

            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                LOGGER.info("Handled requestChannel");
                return Flux
                        .from(payloads)
                        .map(payload -> DefaultPayload.create(
                                "Echo:" + payload.getDataUtf8()
                        ));
            }
        }));
    }

//
//    RSocket rSocket = RSocketFactory.receive()
//            .acceptor(socketAcceptor)
//            .transport(WebsocketServerTransport.create("localhost", 5555))
//            .start()
//            .doOnNext(x -> LOGGER.info("Server started"))
//            .subscribe();

}

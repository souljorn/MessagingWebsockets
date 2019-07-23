package com.rsocket.consumer;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Component
class Producer implements Ordered, ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerApplication.class);

    String message = "Event Information for";

    //Ordering of Messages to be sent to consumer
    @Override
    public int getOrder(){
        return  Ordered.HIGHEST_PRECEDENCE;
    }

    //Notification that will be sent to the browser
    Flux<String>notifications(String name){
        return Flux.fromStream(Stream.generate(()-> message + name + "@" + Instant.now().toString()))
                .delayElements(Duration.ofSeconds(2));
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event){
        final SocketAcceptor socketAcceptor = (connectionSetupPayload, sendingSocket) -> {

            final AbstractRSocket abstractRSocket = new AbstractRSocket() {

                @Override
                public Flux<Payload> requestStream(Payload payload) {
                    final String payloadContent = payload.getDataUtf8();
                    LOGGER.info("Received Initial request from the consumer with payload" + payloadContent);
                    return notifications(payloadContent).map(DefaultPayload::create);
                }
            };
            return Mono.just(abstractRSocket);
        };
        final TcpServerTransport tcpServerTransport = TcpServerTransport.create(7777);
        RSocketFactory.receive().acceptor(socketAcceptor).transport(tcpServerTransport).start().block();
    }
}



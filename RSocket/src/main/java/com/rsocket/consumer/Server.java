package com.rsocket.consumer;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.rsocket.consumer.Server.dataPublisher;

public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    public static final DataPublisher dataPublisher = new DataPublisher();

    private final Disposable server;


    public Server() {


        this.server = RSocketFactory.receive()
                .acceptor((setupPayload, reactiveSocket) -> Mono.just(new RSocketImpl()))
                .transport(TcpServerTransport.create("localhost", 8888))
                .start()

                .single().subscribe();


    }

//    public void dispose() {
//        dataPublisher.complete();
//        this.server.dispose();
//    }


}

class RSocketImpl extends AbstractRSocket {

    /**
     * Handle Request/Response messages
     *
     * @param payload Message payload
     * @return payload response
     */
    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        try {
            return Mono.just(payload); // reflect the payload back to the sender
        } catch (Exception x) {
            return Mono.error(x);
        }
    }

    /**
     * Handle Fire-and-Forget messages
     *
     * @param payload Message payload
     * @return nothing
     */
    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        try {
            dataPublisher.publish(payload); // forward the payload
            return Mono.empty();
        } catch (Exception x) {
            return Mono.error(x);
        }
    }
}

class DataPublisher implements Publisher<Payload> {

    private Subscriber<? super Payload> subscriber;

    @Override
    public void subscribe(Subscriber<? super Payload> subscriber) {
        this.subscriber = subscriber;
    }

    public void publish(Payload payload) {
        if (subscriber != null) {
            subscriber.onNext(payload);
        }
    }

    public void complete() {
        if (subscriber != null) {
           // subscriber.onComplete();
        }
    }

}
class GameController implements Publisher<Payload> {

    private static final Logger LOG = LoggerFactory.getLogger(GameController.class);

    private final String playerName;
    private final List<Long> shots;
    private Subscriber<? super Payload> subscriber;
    private boolean truce = false;


    public GameController(String playerName) {
        this.playerName = playerName;
        this.shots = generateShotList();
    }

    /**
     * Create a random list of time intervals, 0-1000ms
     *
     * @return List of time intervals
     */
    private List<Long> generateShotList() {
        return Flux.range(1, 44)
                .map(x -> (long) Math.ceil(Math.random() * 1000))
                .collectList()
                .block();
    }

    @Override
    public void subscribe(Subscriber<? super Payload> subscriber) {
        this.subscriber = subscriber;
        fireAtWill();
    }

    /**
     * Publish game events asynchronously
     */
    private void fireAtWill() {
        new Thread(() -> {
            for (Long shotDelay : shots) {
                try { Thread.sleep(shotDelay); } catch (Exception x) {}
                if (truce) {
                    break;
                }
                LOG.info("{}: bang!", playerName);
                subscriber.onNext(DefaultPayload.create("bang!"));
            }
            if (!truce) {
                LOG.info("{}: I give up!", playerName);
                subscriber.onNext(DefaultPayload.create("I give up"));
            }
            //subscriber.onComplete();
        }).start();
    }

    /**
     * Process events from the opponent
     *
     * @param payload Payload received from the rSocket
     */
    public void processPayload(Payload payload) {
        String message = payload.getDataUtf8();
        switch (message) {
            case "bang!":
                String result = Math.random() < 0.5 ? "Haha missed!" : "Ow!";
                LOG.info("{}: {}", playerName, result);
                break;
            case "I give up":
                truce = true;
                LOG.info("{}: OK, truce", playerName);
                break;
        }
    }
}

class ReqResClient {

    private final RSocket socket;

    public ReqResClient() {
        this.socket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", 8888))
                .start()
                .block();
    }

    public String callBlocking(String string) {
        return socket
                .requestResponse(DefaultPayload.create(string))
                .map(Payload::getDataUtf8)
                .block();
    }

    public void dispose() {
//        this.socket.dispose();
    }

}


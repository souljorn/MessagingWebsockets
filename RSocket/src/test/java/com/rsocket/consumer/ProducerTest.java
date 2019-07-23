package com.rsocket.consumer;

import io.rsocket.Closeable;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.test.TestRSocket;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.ServerTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.Supplier;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;


public interface ProducerTest {

    default Payload createTestPayload(int metadataPresent) {
        String metadata1;

        switch (metadataPresent % 5) {
            case 0:
                metadata1 = null;
                break;
            case 1:
                metadata1 = "";
                break;
            default:
                metadata1 = "metadata";
                break;
        }
        String metadata = metadata1;

        return DefaultPayload.create("test-data", metadata);
    }



    default RSocket getClient() {
        return getTransportPair().getClient();
    }

    Duration getTimeout();

    TransportPair getTransportPair();

    @DisplayName("makes 1 requestStream request and receives 5 responses")
    @Test
    default void requestStream5() {
        getClient()
                .requestStream(createTestPayload(3))
                .doOnNext(this::assertPayload)
                .take(5)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .expectComplete()
                .verify(getTimeout());
    }
    default void assertPayload(Payload p) {
        TransportPair transportPair = getTransportPair();
        if (!transportPair.expectedPayloadData().equals(p.getDataUtf8())
                || !transportPair.expectedPayloadMetadata().equals(p.getMetadataUtf8())) {
            throw new IllegalStateException("Unexpected payload");
        }
    }

    @AfterEach
    default void close() {
        getTransportPair().dispose();
    }


}
@SpringBootTest
final class TransportPair<T, S extends Closeable> implements Disposable {
    private static final String data = "hello world";
    private static final String metadata = "metadata";

    private final RSocket client;

    private final S server;

    public TransportPair(
            Supplier<T> addressSupplier,
            BiFunction<T, S, ClientTransport> clientTransportSupplier,
            Function<T, ServerTransport<S>> serverTransportSupplier) {

        T address = addressSupplier.get();

        server =
                RSocketFactory.receive()
                        .acceptor((setup, sendingSocket) -> Mono.just(new TestRSocket(data, metadata)))
                        .transport(serverTransportSupplier.apply(address))
                        .start()
                        .block();

        client =
                RSocketFactory.connect()
                        .transport(clientTransportSupplier.apply(address, server))
                        .start()
                        .doOnError(Throwable::printStackTrace)
                        .block();
    }

    @Override
    public void dispose() {
        server.dispose();
    }

    RSocket getClient() {
        return client;
    }

    public String expectedPayloadData() {
        return data;
    }

    public String expectedPayloadMetadata() {
        return metadata;
    }

}
package com.florian935.rsocket.responder;

import com.florian935.rsocket.responder.domain.Message;
import io.rsocket.SocketAcceptor;
import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RSocketClientToServerITest {

    private static RSocketRequester requester;
    private static UsernamePasswordMetadata credentials;
    private static MimeType mimeType;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder,
                                 @LocalRSocketServerPort Integer port,
                                 @Autowired RSocketStrategies strategies) {

        Hooks.onErrorDropped(error -> {});

        final SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientHandler());
        credentials = new UsernamePasswordMetadata("user", "pass");
        requester = builder.tcp("localhost", port);
        mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

        requester = builder
                .setupRoute("shell-client")
                .setupData(UUID.randomUUID().toString())
                .setupMetadata(credentials, mimeType)
                .rsocketStrategies(b ->
                        b.encoder(new SimpleAuthenticationEncoder()))
                .rsocketConnector(connector -> connector.acceptor(responder))
                .tcp("localhost", port);
    }

    @Test
    public void testRequestGetsResponse() {

        final Mono<Message> result = requester
                .route("request-response")
                .data(new Message("TEST", "request"))
                .retrieveMono(Message.class);

        StepVerifier
                .create(result)
                .consumeNextWith(message -> {
                    assertThat(message.getOrigin()).isEqualTo("Server");
                    assertThat(message.getInteraction()).isEqualTo("Response");
                    assertThat(message.getIndex()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    public void testFireAndForget() {

        final Mono<Void> result = requester
                .route("fire-and-forget")
                .data(new Message("TEST", "Fire-And-Forget"))
                .retrieveMono(Void.class);

        StepVerifier
                .create(result)
                .verifyComplete();
    }

    @Test
    public void testRequestGetsStream() {

        final UsernamePasswordMetadata adminCredentials =
            new UsernamePasswordMetadata("admin", "pass");
        final Flux<Message> result = requester
                .route("stream")
                .metadata(adminCredentials, mimeType)
                .data(new Message("TEST", "Stream"))
                .retrieveFlux(Message.class);

        StepVerifier
                .create(result)
                .consumeNextWith(message -> {
                    assertThat(message.getOrigin()).isEqualTo("Server");
                    assertThat(message.getInteraction()).isEqualTo("Stream");
                    assertThat(message.getIndex()).isEqualTo(0L);
                })
                .expectNextCount(3)
                .consumeNextWith(message -> {
                    assertThat(message.getOrigin()).isEqualTo("Server");
                    assertThat(message.getInteraction()).isEqualTo("Stream");
                    assertThat(message.getIndex()).isEqualTo(4L);
                })
                .thenCancel()
                .verify();
    }

    @Test
    public void testStreamGetsStream() {

        final UsernamePasswordMetadata adminCredentials =
                new UsernamePasswordMetadata("admin", "pass");
        final Mono<Long> setting1 = Mono.just(ofSeconds(6)).delayElement(ofSeconds(0)).map(Duration::getSeconds);
        final Mono<Long> setting2 = Mono.just(ofSeconds(6)).delayElement(ofSeconds(9)).map(Duration::getSeconds);
        final Flux<Long> settings = Flux.concat(setting1, setting2);

        final Flux<Message> result = requester
                .route("channel")
                .metadata(adminCredentials, mimeType)
                .data(settings)
                .retrieveFlux(Message.class);

        StepVerifier
                .create(result)
                .consumeNextWith(message -> {
                    assertThat(message.getOrigin()).isEqualTo("Server");
                    assertThat(message.getInteraction()).isEqualTo("Channel");
                    assertThat(message.getIndex()).isEqualTo(0L);
                })
                .consumeNextWith(message -> {
                    assertThat(message.getOrigin()).isEqualTo("Server");
                    assertThat(message.getInteraction()).isEqualTo("Channel");
                    assertThat(message.getIndex()).isEqualTo(0L);
                })
                .thenCancel()
                .verify();
    }

    @AfterAll
    public static void tearDownOnce() {

        requester.rsocketClient().dispose();
    }
}

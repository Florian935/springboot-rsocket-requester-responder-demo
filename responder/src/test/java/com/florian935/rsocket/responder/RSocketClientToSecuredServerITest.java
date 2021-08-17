package com.florian935.rsocket.responder;

import com.florian935.rsocket.responder.domain.Message;
import io.rsocket.SocketAcceptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION;

@SpringBootTest
public class RSocketClientToSecuredServerITest {

    private static RSocketRequester requester;
    private static UsernamePasswordMetadata credentials;
    private static MimeType mimeType;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder,
                                 @LocalRSocketServerPort Integer port,
                                 @Autowired RSocketStrategies strategies) {

        Hooks.onErrorDropped(error -> {});

        final SocketAcceptor responder =
                RSocketMessageHandler.responder(strategies, new ClientHandler());
        mimeType = MimeTypeUtils.parseMimeType(MESSAGE_RSOCKET_AUTHENTICATION.getString());

        // ****** The user 'test' is NOT in the required 'USER' role ! ******
        credentials = new UsernamePasswordMetadata("test", "pass");

        requester = builder
                .setupRoute("shell-client")
                .setupData(UUID.randomUUID().toString())
                .setupMetadata(credentials, mimeType)
                .rsocketStrategies(b -> b.encoder(new SimpleAuthenticationEncoder()))
                .rsocketConnector(connector -> connector.acceptor(responder))
                .tcp("localhost", port);
    }

    @Test
    public void testFireAndForget() {

        final Mono<Void> result = requester
                .route("fire-and-forget")
                .data(new Message("TEST", "Fire-And-Forget"))
                .retrieveMono(Void.class);

        StepVerifier
                .create(result)
                .verifyErrorMessage("Invalid Credentials");
    }

    @AfterAll
    public static void tearDownOnce() {

        requester.rsocketClient().dispose();
    }
}

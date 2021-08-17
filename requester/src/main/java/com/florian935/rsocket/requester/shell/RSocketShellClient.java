package com.florian935.rsocket.requester.shell;

import com.florian935.rsocket.requester.domain.Message;
import com.florian935.rsocket.requester.handler.ClientHandler;
import io.rsocket.SocketAcceptor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION;
import static java.time.Duration.ofSeconds;
import static lombok.AccessLevel.PRIVATE;

@ShellComponent
@Slf4j
@FieldDefaults(level = PRIVATE)
@RequiredArgsConstructor
public class RSocketShellClient {

    final static String REQUEST_RESPONSE_ROUTE = "request-response";
    final static String FIRE_AND_FORGET_ROUTE = "fire-and-forget";
    final static String STREAM_ROUTE = "stream";
    final static String CHANNEL_ROUTE = "channel";
    final static String CLIENT = "Client";
    final static String REQUEST_RESPONSE = "Request";
    final static String FIRE_AND_FORGET = "Fire-And-Forget";
    final static String STREAM = "Stream";
    static Disposable disposable;
    final static String SHELL_CLIENT_ROUTE = "shell-client";
    final static String HOST = "localhost";
    static final MimeType SIMPLE_AUTH = MimeTypeUtils
            .parseMimeType(MESSAGE_RSOCKET_AUTHENTICATION.getString());
    final static int PORT = 7000;
    final static String CLIENT_ID = UUID.randomUUID().toString();

    RSocketRequester rSocketRequester;
    final RSocketRequester.Builder builder;
    final RSocketStrategies rSocketStrategies;

    @ShellMethod("Login with your username and password")
    void login(String username,
               String password) {

        log.info("Connecting using client ID: {} and username: {}", CLIENT_ID, username);
        final SocketAcceptor responder =
                RSocketMessageHandler.responder(rSocketStrategies, new ClientHandler());
        final UsernamePasswordMetadata user = new UsernamePasswordMetadata(username, password);

        rSocketRequester = builder
                .setupRoute(SHELL_CLIENT_ROUTE)
                .setupData(CLIENT_ID)
                .setupMetadata(user, SIMPLE_AUTH)
                .rsocketStrategies(rsocketBuilder ->
                        rsocketBuilder.encoder(new SimpleAuthenticationEncoder()))
                .rsocketConnector(rSocketConnector -> rSocketConnector.acceptor(responder))
                .tcp(HOST, PORT);

        rSocketRequester.rsocketClient()
                .source()
                .doOnError(error -> log.warn("Connection CLOSED"))
                .doFinally(consumer -> log.info("Client DISCONNECTED"))
                .subscribe();
    }

    @PreDestroy
    @ShellMethod("Logout and close your connection")
    public void logout() {

        if (userIsLoggedIn()) {
            stop();
            rSocketRequester.rsocketClient().dispose();
            log.info("Logged out.");
        }
    }

    private boolean userIsLoggedIn() {

        if (Objects.isNull(rSocketRequester) || rSocketRequester.rsocketClient().isDisposed()) {
            log.info("No connection. Did you login?");
            return false;
        }

        return true;
    }

    @ShellMethod("Send one request. One response will be printed.")
    void requestResponse() {

        log.info("\nSending one request. Waiting for one response ...");

        rSocketRequester
                .route(REQUEST_RESPONSE_ROUTE)
                .data(new Message(CLIENT, REQUEST_RESPONSE))
                .retrieveMono(Message.class)
                .subscribe(
                        message -> log.info("\nResponse was: {}", message),
                        error -> log.error("An error occurred: {}", error.getMessage()),
                        () -> log.info("Request-Response interaction completed ...")
                );
    }

    @ShellMethod("Send one request. No response will be returned")
    void fireAndForget() {

        log.info("\nFire-And-Forget. Sending one request. Expect no response (check server log) ...");

        this.rSocketRequester
                .route(FIRE_AND_FORGET_ROUTE)
                .data(new Message(CLIENT, FIRE_AND_FORGET))
                .send()
                .subscribe(
                        message -> log.info("This message never appear because it's the fire-and-forget mode."),
                        error -> log.error("An error occurred: {}", error.getMessage()),
                        () -> log.info("Fire-And-Forget interaction completed ...")
                );
    }

    @ShellMethod("Send one request. Many responses (stream) will be printed.")
    void stream() {

        log.info("\nRequest-Stream. Sending one request. Waiting for unlimited responses (Stop process to quit) ...");

        disposable = rSocketRequester
                .route(STREAM_ROUTE)
                .data(new Message(CLIENT, STREAM))
                .retrieveFlux(Message.class)
                .doOnCancel(() -> log.info("Stream interaction stopped ..."))
                .subscribe(
                        message -> log.info("Response received: {}", message),
                        error -> log.error("An error occurred: {}", error.getMessage()),
                        () -> log.info("This message never appear because the stream is never completed (unlimited)")
                );
    }

    @ShellMethod("Stream some settings to the server. Stream of responses will be printed ")
    public void channel() {

        final Mono<Duration> setting1 = Mono.just(ofSeconds(1));
        final Mono<Duration> setting2 = Mono.just(ofSeconds(3)).delayElement(ofSeconds(5));
        final Mono<Duration> setting3 = Mono.just(ofSeconds(5)).delayElement(ofSeconds(15));

        final Flux<Duration> settings = Flux.concat(setting1, setting2, setting3)
                .doOnNext(d -> log.info("\nSending setting for {} - second interval.\n", d.getSeconds()));

        disposable = this.rSocketRequester
                .route(CHANNEL_ROUTE)
                .data(settings)
                .retrieveFlux(Message.class)
                .doOnCancel(() -> log.info("Channel interaction stopped ..."))
                .subscribe(
                        message -> log.info("Received: {} \n(Type 'stop' to stop.)", message),
                        error -> log.error("An error occurred: {}", error.getMessage()),
                        () -> log.info("Channel interaction completed ...")
                );
    }


    @ShellMethod("Stop streaming messages from the server.")
    void stop() {
        if (Objects.nonNull(disposable)) {
            disposable.dispose();
        }
    }
}

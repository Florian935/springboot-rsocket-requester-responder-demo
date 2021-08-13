package com.florian935.rsocket.requester.shell;

import com.florian935.rsocket.requester.domain.Message;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

import static java.time.Duration.ofSeconds;
import static lombok.AccessLevel.PRIVATE;

@ShellComponent
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RSocketShellClient {

    static String REQUEST_RESPONSE_ROUTE = "request-response";
    static String FIRE_AND_FORGET_ROUTE = "fire-and-forget";
    static String STREAM_ROUTE = "stream";
    static String CHANNEL_ROUTE = "channel";
    static String CLIENT = "Client";
    static String REQUEST_RESPONSE = "Request";
    static String FIRE_AND_FORGET = "Fire-And-Forget";
    static String STREAM = "Stream";
    static Disposable disposable;


    RSocketRequester rSocketRequester;

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

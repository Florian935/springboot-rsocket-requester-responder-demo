package com.florian935.rsocket.requester.shell;

import com.florian935.rsocket.requester.domain.Message;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.Disposable;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@ShellComponent
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RSocketShellClient {

    static String REQUEST_RESPONSE_ROUTE = "request-response";
    static String FIRE_AND_FORGET_ROUTE = "fire-and-forget";
    static String STREAM_ROUTE = "stream";
    static String CLIENT = "Client";
    static String REQUEST_RESPONSE = "Request";
    static String FIRE_AND_FORGET = "Fire-And-Forget";
    static String STREAM = "Stream";
    static Disposable disposable;


    RSocketRequester rSocketRequester;

    @ShellMethod("Send one request. One response will be printed.")
    void requestResponse() {

        log.info("\nSending one request. Waiting for one response ...");
        final Message message = rSocketRequester
                .route(REQUEST_RESPONSE_ROUTE)
                .data(new Message(CLIENT, REQUEST_RESPONSE))
                .retrieveMono(Message.class)
                .block();

        log.info("\nResponse was: {}", message);
    }

    @ShellMethod("Send one request. No response will be returned")
    void fireAndForget() {

        log.info("\nFire-And-Forget. Sending one request. Expect no response (check server log) ...");

        this.rSocketRequester
                .route(FIRE_AND_FORGET_ROUTE)
                .data(new Message(CLIENT, FIRE_AND_FORGET))
                .send()
                .block();
    }

    @ShellMethod("Send one request. Many responses (stream) will be printed.")
    void stream() {

        log.info("\nRequest-Stream. Sending one request. Waiting for unlimited responses (Stop process to quit) ...");

        disposable = rSocketRequester
                .route(STREAM_ROUTE)
                .data(new Message(CLIENT, STREAM))
                .retrieveFlux(Message.class)
                .subscribe(message -> log.info("Response received: {}", message));
    }

    @ShellMethod("Stop streaming messages from the server.")
    void stop() {
        if (Objects.nonNull(disposable)) {
            disposable.dispose();
        }
    }
}

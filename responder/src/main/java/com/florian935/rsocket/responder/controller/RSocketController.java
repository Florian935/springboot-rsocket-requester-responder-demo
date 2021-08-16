package com.florian935.rsocket.responder.controller;

import com.florian935.rsocket.responder.domain.Message;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static lombok.AccessLevel.PRIVATE;

@Controller
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RSocketController {

    static final String SERVER = "Server";
    static final String RESPONSE = "Response";
    static final String STREAM = "Stream";
    static final String CHANNEL = "Channel";

    List<RSocketRequester> CLIENTS = new ArrayList<>();

    @ConnectMapping("shell-client")
    void connectShellClientAndAskForTelemetry(
            RSocketRequester requester,
            @Payload String client) {

        logRequester(requester, client);

        requester.route("client-status")
                .data("OPEN")
                .retrieveFlux(String.class)
                .doOnNext(s -> log.info("Client: {} Free Memory: {}.", client, s))
                .subscribe();

    }

    private void logRequester(RSocketRequester requester, String payload) {

        requester
                .rsocket()
                .onClose()
                .doFirst(() -> {
                    log.info("Client: {} CONNECTED.", payload);
                    CLIENTS.add(requester);
                })
                .doOnError(error -> {
                    log.warn("Channel to client {} CLOSED", payload);
                })
                .doFinally(consumer -> {
                    CLIENTS.remove(requester);
                    log.info("Client {} DISCONNECTED", payload);
                })
                .subscribe();
    }

    @MessageMapping("request-response")
    Mono<Message> requestResponse(@Payload final Message message) {

        log.info("Received request-response request: {}", message);

        return Mono.just(new Message(SERVER, RESPONSE));
    }

    @PreAuthorize("hasRole('USER')")
    @MessageMapping("fire-and-forget")
    Mono<Void> fireAndForget(@Payload final Message message,
                             @AuthenticationPrincipal UserDetails user) {

        log.info("Received fire-and-forget request: {}", message);
        log.info("Fire-And-Forget initiated by '{}' in the role '{}'",
                user.getUsername(),
                user.getAuthorities());

        return Mono.empty();
    }

    @MessageMapping("stream")
    Flux<Message> stream(@Payload final Message message) {

        log.info("Received stream request: {}", message);

        return Flux.
                interval(ofSeconds(1))
                .map(index -> new Message(SERVER, STREAM, index))
                .log();
    }

    @MessageMapping("channel")
    Flux<Message> channel(@Payload final Flux<Integer> settings) {

        return settings
                .doOnNext(setting -> log.info("\nFrequency setting is {} second(s). \n", setting))
                .switchMap(setting -> Flux.interval(ofSeconds(setting))
                                            .map(index -> new Message(SERVER, CHANNEL, index)))
                .log();
    }
}

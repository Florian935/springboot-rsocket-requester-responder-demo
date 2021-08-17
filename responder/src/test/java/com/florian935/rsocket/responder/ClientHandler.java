package com.florian935.rsocket.responder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import reactor.core.publisher.Flux;

import static java.time.Duration.ofSeconds;

@Slf4j
public class ClientHandler {

    @MessageMapping("client-status")
    public Flux<String> statusUpdate(String status) {

        log.info("Connection {}", status);
        return Flux.interval(ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }
}

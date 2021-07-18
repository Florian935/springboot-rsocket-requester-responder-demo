package com.florian935.rsocket.responder.controller;

import com.florian935.rsocket.responder.domain.Message;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import static lombok.AccessLevel.PRIVATE;

@Controller
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RSocketController {

    static final String SERVER = "Server";
    static final String RESPONSE = "Response";
    static final String STREAM = "Stream";
    static final String CHANNEL = "Channel";

    @MessageMapping("request-response")
    Mono<Message> requestResponse(@Payload final Message request) {
        log.info("Received request-response request: {}", request);

        return Mono.just(new Message(SERVER, RESPONSE));
    }
}

package com.florian935.rsocket.responder.controller;

import com.florian935.rsocket.responder.domain.Message;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import static lombok.AccessLevel.PRIVATE;

@Controller
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RsocketController {

    static String SERVER = "Server";
    static String RESPONSE = "Response";
    static String STREAM = "Stream";
    static String CHANNEL = "Channel";

    @MessageMapping("request-response")
    Message requestResponse(Message message) {

        log.info("Received request-response request: {}", message);

        return new Message(SERVER, RESPONSE);
    }
}

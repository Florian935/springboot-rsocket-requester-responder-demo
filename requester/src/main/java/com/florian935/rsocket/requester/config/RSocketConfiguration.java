package com.florian935.rsocket.requester.config;

import com.florian935.rsocket.requester.handler.ClientHandler;
import io.rsocket.SocketAcceptor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
//@Configuration
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RSocketConfiguration {

    static String HOST = "localhost";
    static int PORT = 7000;
    static String SHELL_CLIENT_ROUTE = "shell-client";

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder,
                                      RSocketStrategies rSocketStrategies) {

        final String client = UUID.randomUUID().toString();
        log.info("Connecting using client ID: {}", client);

        final SocketAcceptor responder =
                RSocketMessageHandler.responder(rSocketStrategies, new ClientHandler());

        final RSocketRequester rSocketRequester = builder
                .setupRoute(SHELL_CLIENT_ROUTE)
                .setupData(client)
                .rsocketStrategies(rSocketStrategies)
                .rsocketConnector(rSocketConnector -> rSocketConnector.acceptor(responder))
                .tcp(HOST, PORT);

        rSocketRequester.rsocketClient()
                .source()
                .doOnError(error -> log.warn("Connection CLOSED"))
                .doFinally(consumer -> log.info("Client DISCONNECTED"))
                .subscribe();

        return rSocketRequester;
    }
}

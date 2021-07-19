package com.florian935.rsocket.requester.config;

import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;

import static lombok.AccessLevel.PRIVATE;

@Configuration
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RSocketConfiguration {

    static String HOST = "localhost";
    static int PORT = 7000;

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {

        return builder.tcp(HOST, PORT);
    }
}

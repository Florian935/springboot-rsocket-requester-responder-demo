package com.florian935.rsocket.responder.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    ObjectMapper objectMapper() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ALLOW_SINGLE_QUOTES, true);

        return objectMapper;
    }
}

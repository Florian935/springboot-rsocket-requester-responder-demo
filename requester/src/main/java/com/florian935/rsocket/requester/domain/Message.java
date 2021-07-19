package com.florian935.rsocket.requester.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class Message {

    String origin;
    String interaction;
    long index;
    long created = Instant.now().getEpochSecond();

    public Message(String origin, String interaction) {

        this.origin = origin;
        this.interaction = interaction;
        this.index = 0;
    }

    public Message(String origin, String interaction, long index) {

        this.origin = origin;
        this.interaction = interaction;
        this.index = index;
    }
}

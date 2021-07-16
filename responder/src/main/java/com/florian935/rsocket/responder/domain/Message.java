package com.florian935.rsocket.responder.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = PRIVATE)
public class Message {

    String origin;
    String interactionMode;
    long index;
    long created = Instant.now().getEpochSecond();

    public Message(String origin, String interactionMode) {
        this.origin = origin;
        this.interactionMode = interactionMode;
        this.index = 0;
    }

    public Message(String origin, String interactionMode, long index) {
        this.origin = origin;
        this.interactionMode = interactionMode;
        this.index = index;
    }
}

package com.florian935.rsocket.responder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ResponderApplication {

	public static void main(String[] args) {

		Hooks.onErrorDropped(error -> {});
		SpringApplication.run(ResponderApplication.class, args);
	}

}

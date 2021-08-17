package com.florian935.rsocket.requester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class RequesterApplication {

	public static void main(String[] args) {

		Hooks.onErrorDropped(error -> {});
		SpringApplication.run(RequesterApplication.class, args);
	}

}

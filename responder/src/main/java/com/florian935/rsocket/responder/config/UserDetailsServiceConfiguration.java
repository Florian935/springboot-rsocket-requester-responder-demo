package com.florian935.rsocket.responder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Configuration
public class UserDetailsServiceConfiguration {

    @Bean
    MapReactiveUserDetailsService authentication() {

        final List<UserDetails> users = List.of(buildUser(), buildAdmin());

        return new MapReactiveUserDetailsService(users);
    }

    private UserDetails buildUser() {

        return User
                .withUsername("user")
                .password("pass")
                .roles("USER")
                .build();
    }

    private UserDetails buildAdmin() {

        return User
                .withUsername("admin")
                .password("pass")
                .roles("ADMIN")
                .build();
    }
}

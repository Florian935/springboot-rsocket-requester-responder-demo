package com.florian935.rsocket.responder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class UserDetailsServiceConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    MapReactiveUserDetailsService authentication() {

        final List<UserDetails> users = List.of(buildUser(), buildAdmin());

        return new MapReactiveUserDetailsService(users);
    }

    private UserDetails buildUser() {

        return User
                .withUsername("user")
                .password(passwordEncoder().encode("pass"))
                .roles("USER")
                .build();
    }

    private UserDetails buildAdmin() {

        return User
                .withUsername("admin")
                .password(passwordEncoder().encode("pass"))
                .roles("ADMIN")
                .build();
    }
}

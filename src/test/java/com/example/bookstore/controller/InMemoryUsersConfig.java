package com.example.bookstore.controller;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;


@TestConfiguration
public class InMemoryUsersConfig {
    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{noop}admin123").roles("ADMIN","USER").build(),
                User.withUsername("user").password("{noop}user123").roles("USER").build(),
                User.withUsername("guest").password("{noop}guest123").roles("GUEST").build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // fine for tests
    }
}
package com.example.bookstore.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private List<UserSpec> users;

    @Data
    public static class UserSpec {
        private String username;
        private String password;
        private List<String> roles;
    }
}

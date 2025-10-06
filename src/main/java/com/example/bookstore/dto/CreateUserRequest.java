package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  CreateUserRequest {

    @NotBlank
    @Size(min = 8, max = 30, message = "Username must be at least 8 characters long")
    private String username;

    @NotBlank
    @Size(min = 8, max = 30, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank
    @Size(min = 8, max = 30, message = "Confirm password must be at least 8 characters long")
    private String confirmPassword;

    @NotEmpty(message = "At least one role must be specified")
    private Set<String> roles;
}


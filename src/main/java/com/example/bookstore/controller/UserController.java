package com.example.bookstore.controller;

import com.example.bookstore.dto.CreateUserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.dto.UserStatusRequest;
import com.example.bookstore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userService.createUser(req);
    }

    @GetMapping("/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse get(@PathVariable String name) {
        return userService.getUserByName(name);
    }

    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public void activateUser(@PathVariable Long id, @RequestBody UserStatusRequest request) {
        userService.activateUser(id, request);
    }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public void deactivateUser(@PathVariable Long id, @RequestBody UserStatusRequest request) {
        userService.deactivateUser(id, request);
    }
}

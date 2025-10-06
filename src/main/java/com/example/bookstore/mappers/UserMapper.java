package com.example.bookstore.mappers;


import com.example.bookstore.domain.User;
import com.example.bookstore.dto.CreateUserRequest;
import com.example.bookstore.dto.UserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .description(user.getDescription())
                .build();
    }

    public static User fromCreateRequest(CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEnabled(true);
        newUser.setDescription("Created via API");
        return newUser;
    }
}
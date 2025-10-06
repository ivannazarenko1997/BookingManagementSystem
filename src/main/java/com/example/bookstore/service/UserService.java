package com.example.bookstore.service;

import com.example.bookstore.domain.User;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.dto.UserStatusRequest;

public interface UserService {
  UserResponse createUser(com.example.bookstore.dto.CreateUserRequest req);
  void activateUser(Long id, UserStatusRequest request);
  void deactivateUser(Long id, UserStatusRequest request);
  User findByUserName(  String userName);
  UserResponse getUserByName(String userName);
}
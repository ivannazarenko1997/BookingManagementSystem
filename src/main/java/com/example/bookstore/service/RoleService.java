package com.example.bookstore.service;

import com.example.bookstore.domain.Role;
public interface RoleService {
  Role findByName(String roleName);
}

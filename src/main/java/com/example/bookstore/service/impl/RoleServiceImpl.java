package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Role;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.RoleRepository;
import com.example.bookstore.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;



    @Override
    public Role findByName(String roleName) {
        return roleRepository.findByName(roleName).orElseThrow(() -> new BookStoreException(
                "Unknown role: " + roleName));
    }
}

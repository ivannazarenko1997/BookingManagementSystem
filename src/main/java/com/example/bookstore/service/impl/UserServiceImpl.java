package com.example.bookstore.service.impl;

import com.example.bookstore.domain.Role;
import com.example.bookstore.domain.User;
import com.example.bookstore.domain.UserRole;
import com.example.bookstore.dto.CreateUserRequest;
import com.example.bookstore.dto.UserResponse;
import com.example.bookstore.dto.UserStatusRequest;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.mappers.UserMapper;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.repository.UserRoleRepository;
import com.example.bookstore.service.RoleService;
import com.example.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        User findedUser = userRepository.findByUsernameExplicit(request.getUsername());
        if (findedUser != null) {
            throw new BookStoreException("User allready exists with login=" + request.getUsername());
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BookStoreException("User passwords not unigue for" + request.getUsername());
        }

        User newUser = UserMapper.fromCreateRequest(request);
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(newUser);

        for (String roleName : request.getRoles()) {
            Role role = roleService.findByName(roleName);
            UserRole newRole = new UserRole();
            newRole.setUser(savedUser);
            newRole.setRole(role);
            userRoles.save(newRole);
        }
        return UserMapper.toDto(newUser);
    }

    @Override
    @Transactional
    public void activateUser(Long id, UserStatusRequest request) {
        User findedUser = findByUserId(id);
        findedUser.setEnabled(true);
        findedUser.setDescription(request.getReason() == null ? "Activated by admin" : request.getReason());
        userRepository.save(findedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id, UserStatusRequest request) {
        User findedUser = findByUserId(id);
        findedUser.setEnabled(false);
        findedUser.setDescription(request.getReason() == null ? "Deactivated by admin" : request.getReason());
        userRepository.save(findedUser);
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUsernameExplicit(userName);
    }

    @Override
    public UserResponse getUserByName(String userName) {
        User findedUser = findByUserName(userName);
        if (findedUser == null) {
            throw new BookStoreException("User with login not found=" + userName);
        }
        return UserMapper.toDto(findedUser);
    }

    public User findByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BookStoreException("User by id not found: " + userId));
    }
}

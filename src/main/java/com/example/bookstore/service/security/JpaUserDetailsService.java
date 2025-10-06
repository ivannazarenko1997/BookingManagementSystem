package com.example.bookstore.service.security;

import com.example.bookstore.domain.User;
import com.example.bookstore.exception.BookStoreException;
import com.example.bookstore.repository.RoleRepository;
import com.example.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {
    private final UserService userService;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User findedUser = userService.findByUserName(username);

        if (findedUser == null) {
            throw new BookStoreException("User not found: " + username);
        }
        var roles = roleRepository.findRoleNamesByUserId(findedUser.getId()).stream()
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getName()))
                .collect(Collectors.toSet());
        return org.springframework.security.core.userdetails.User
                .withUsername(findedUser.getUsername())
                .password(findedUser.getPasswordHash())
                .authorities(roles)
                .disabled(!findedUser.isEnabled())
                .build();
    }
}

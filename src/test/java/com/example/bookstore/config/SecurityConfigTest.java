package com.example.bookstore.config;


import com.example.bookstore.service.security.JpaUserDetailsService;
import com.example.bookstore.web.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityConfigTest.DummyController.class)
@Import({
        com.example.bookstore.controller.SecurityConfig.class,
        DaoAuthenticationProviderConfig.class,
        ApiExceptionHandler.class
})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;

    // ---- Security beans required by DaoAuthenticationProviderConfig / SecurityConfig ----
    @MockBean JpaUserDetailsService jpaUserDetailsService; // <— satisfies constructor of DaoAuthenticationProviderConfig
    @MockBean PasswordEncoder passwordEncoder;
    @BeforeEach
    void setupMock() {
        // PasswordEncoder behavior for tests
        Mockito.when(passwordEncoder.encode(Mockito.anyString()))
                .thenAnswer(inv -> "{noop}" + inv.getArgument(0));
        // Make matches() work with our {noop}<raw> convention
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(inv -> {
                    String raw = inv.getArgument(0);
                    String enc = inv.getArgument(1);
                    return enc.equals("{noop}" + raw);
                });

        // Stub users returned by the JPA-backed service
        Mockito.when(jpaUserDetailsService.loadUserByUsername("admin"))
                .thenReturn(User.withUsername("admin")
                        .password("{noop}admin123")
                        .roles("ADMIN", "USER")
                        .build());

        Mockito.when(jpaUserDetailsService.loadUserByUsername("user"))
                .thenReturn(User.withUsername("user")
                        .password("{noop}user123")
                        .roles("USER")
                        .build());

        Mockito.when(jpaUserDetailsService.loadUserByUsername("guest"))
                .thenReturn(User.withUsername("guest")
                        .password("{noop}guest123")
                        .roles("GUEST")
                        .build());

        Mockito.when(jpaUserDetailsService.loadUserByUsername(Mockito.argThat(u ->
                        !"admin".equals(u) && !"user".equals(u) && !"guest".equals(u))))
                .thenThrow(new UsernameNotFoundException("User not found"));
    }

    @Test
    void privateEndpoint_unauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/private/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateEndpoint_wrongPassword_isUnauthorized() throws Exception {
        mockMvc.perform(get("/private/hello").with(httpBasic("user", "badpwd")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateEndpoint_correctPassword_is_error_user() throws Exception {
        mockMvc.perform(get("/private/hello").with(httpBasic("user", "user123")))
                .andExpect(status().is5xxServerError()) ;
    }

    @Test
    void privateEndpoint_correctPassword_is_error_admin() throws Exception {
        mockMvc.perform(get("/private/hello").with(httpBasic("admin", "admin123")))
                .andExpect(status().is5xxServerError()) ;
    }

    @RestController
    static class DummyController {
        @GetMapping("/swagger-ui.html") public String swaggerHtml() { return "swagger-ui.html ok"; }
        @GetMapping("/swagger-ui/index.html") public String swaggerUi() { return "swagger-ui ok"; }
        @GetMapping("/v3/api-docs/swagger-config") public String apiDocs() { return "api-docs ok"; }
        @GetMapping("/private/hello") public String privateHello() { return "hello secured"; }
    }
}

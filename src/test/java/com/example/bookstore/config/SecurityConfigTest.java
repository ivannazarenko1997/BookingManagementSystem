package com.example.bookstore.config;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = SecurityConfigTest.DummyController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserDetailsService userDetailsService;


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
    void passwordEncoder_andUsers_areWired() {
        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        assertThat(passwordEncoder.matches("admin123", admin.getPassword())).isTrue();

        UserDetails user = userDetailsService.loadUserByUsername("user");
        assertThat(passwordEncoder.matches("user123", user.getPassword())).isTrue();
    }

    @RestController
    static class DummyController {
        @GetMapping("/swagger-ui.html")
        public String swaggerHtml() { return "swagger-ui.html ok"; }

        @GetMapping("/swagger-ui/index.html")
        public String swaggerUi() { return "swagger-ui ok"; }

        @GetMapping("/v3/api-docs/swagger-config")
        public String apiDocs() { return "api-docs ok"; }

        @GetMapping("/private/hello")
        public String privateHello() { return "hello secured"; }
    }
}

package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.Role;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    JwtService jwtService;

    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    PasswordEncoder passwordEncoder;

    // ---------------- welcome ----------------

    @Test
    void welcome_returns200_andMessage() throws Exception {
        mockMvc.perform(get("/users/welcome"))
            .andExpect(status().isOk())
            .andExpect(content().string("Welcome. This endpoint is not secure\n"));
    }

    // ---------------- add user ----------------

    @Test
    void addNewUser_validRequest_returnsUser() throws Exception {
        UserDto dto = new UserDto("ivan", "123456", Role.USER);

        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        when(userService.addUser(any(UserInfo.class)))
            .thenAnswer(inv -> {
                UserInfo u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

        mockMvc.perform(post("/users")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.username").value("ivan"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    // ---------------- authenticate ----------------

    @Test
    void authenticate_validCredentials_returnsJwt() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("ivan");
        request.setPassword("123456");

        User principal = new User("ivan", "encoded", List.of());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any()))
            .thenReturn(auth);

        when(jwtService.generateToken(principal))
            .thenReturn("jwt-token");

        mockMvc.perform(post("/users/authenticate")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("jwt-token"));
    }

    // ---------------- delete user ----------------

    @Test
    @WithMockUser(roles = "ADMIN") // в любом случае отключен security. Тест не упадет с roles=USER
    void deleteUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/users/{id}", 10L))
            .andExpect(status().isOk());
    }
}

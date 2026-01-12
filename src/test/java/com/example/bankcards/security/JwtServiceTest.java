package com.example.bankcards.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY =
        "my-super-secret-key-my-super-secret-key"; // >= 256 bit
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_shouldReturnValidJwt() {
        User user = new User(
            "ivan",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        User user = new User(
            "ivan",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("ivan");
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        User user = new User(
            "ivan",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, user);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForAnotherUser() {
        User user1 = new User(
            "ivan",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        User user2 = new User(
            "petr",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(user1);

        boolean valid = jwtService.isTokenValid(token, user2);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenExpired_shouldReturnTrueForExpiredToken() {
        User user = new User(
            "ivan",
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // manually create expired token
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
            .setSubject(user.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis() - 10_000))
            .setExpiration(new Date(System.currentTimeMillis() - 5_000))
            .signWith(key)
            .compact();

        boolean expired = jwtService.isTokenExpired(expiredToken);

        assertThat(expired).isTrue();
    }
}

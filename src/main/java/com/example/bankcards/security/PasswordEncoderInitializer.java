package com.example.bankcards.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class PasswordEncoderInitializer {

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public PasswordEncoderInitializer(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void encodePasswords() {
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);
        System.out.println("Original password: " + password);
        System.out.println("Encoded password: " + encodedPassword);
    }
}

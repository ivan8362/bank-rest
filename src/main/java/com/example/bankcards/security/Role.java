package com.example.bankcards.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role //implements GrantedAuthority
{
    USER,
    ADMIN;

//    private String value;
//
//    Role(String value) {
//        this.value = value;
//    }
//
//    public String getValue() {
//        return this.value;
//    }
//
//    @Override
//    public String getAuthority() {
//        return name();
//    }
}

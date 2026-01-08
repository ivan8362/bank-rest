package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.repository.UserInfoRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserInfoRepository repository;

    private final PasswordEncoder passwordEncoder;

    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "user added to system ";
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return repository.findByUsername(name)
            .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}

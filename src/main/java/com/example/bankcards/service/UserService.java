package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public UserInfo addUser(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        return repository.save(userInfo);
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return repository.findByUsername(name)
            .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s not found", name)));
    }

    public void deleteUser(Long userId) {
        repository.deleteById(userId);
        LOGGER.info("Successfully deleted user with id: {}", userId);
    }
}

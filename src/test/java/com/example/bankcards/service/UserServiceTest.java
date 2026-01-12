package com.example.bankcards.service;

import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserInfoRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void createUser_shouldSaveUserWithEncodedPassword() {
        UserInfo user = new UserInfo();
        user.setUsername("ivan");
        user.setPassword("plain");

        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(UserInfo.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        UserInfo saved = userService.addUser(user);

        assertThat(saved.getPassword()).isEqualTo("encoded");

        verify(passwordEncoder).encode("plain");
        verify(userRepository).save(user);
    }

    @Test
    void findByUsername_shouldReturnUser_whenExists() {
        UserInfo user = new UserInfo();
        user.setUsername("ivan");

        when(userRepository.findByUsername("ivan"))
            .thenReturn(Optional.of(user));

        UserInfo result = (UserInfo) userService.loadUserByUsername("ivan");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findByUsername_shouldThrowException_whenNotExists() {
        when(userRepository.findByUsername("ivan"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("ivan"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("ivan");
    }
}

package com.example.bankcards.service;

import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.repository.UserInfoRepository;
import com.example.bankcards.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserServicePostgresTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
        .withDatabaseName("testdb")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserService userService;

    @Autowired
    UserInfoRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void addUser_passwordIsEncoded_andUserIsSaved() {
        UserInfo user = new UserInfo();
        user.setUsername("ivan");
        user.setPassword("plain-password");
        user.setRole(Role.USER);

        UserInfo saved = userService.addUser(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo("plain-password");
        assertThat(passwordEncoder.matches("plain-password", saved.getPassword()))
            .isTrue();
    }

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("alex");
        userInfo.setPassword("1234");
        userInfo.setRole(Role.USER);
        UserInfo user = userService.addUser(userInfo);

        var userDetails = userService.loadUserByUsername("alex");

        assertThat(userDetails.getUsername()).isEqualTo("alex");
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    void loadUserByUsername_userNotFound_throwsException() {
        assertThatThrownBy(() -> userService.loadUserByUsername("missing"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("missing");
    }

    @Test
    void deleteUser_existingUser_userIsDeleted() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("to_delete");
        userInfo.setPassword("password");
        userInfo.setRole(Role.USER);
        UserInfo user = userService.addUser(userInfo);

        userService.deleteUser(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}

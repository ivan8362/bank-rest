package com.example.bankcards.repository;

import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.security.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserInfoRepositoryTest {

    @Autowired
    private UserInfoRepository userRepository;

    @Test
    @DisplayName("Should find user by existing username")
    void findByUsername_shouldReturnUser_whenUserExists() {
        // given
        String username = "testWorker";
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword("securePass");
        user.setRole(Role.USER);
        userRepository.save(user);

        // when
        Optional<UserInfo> found = userRepository.findByUsername(username);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(username);
        assertThat(found.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Should return empty Optional when username does not exist")
    void findByUsername_shouldReturnEmpty_whenUserDoesNotExist() {
        // given
        String nonExistentUsername = "ghost";

        // when
        Optional<UserInfo> found = userRepository.findByUsername(nonExistentUsername);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should correctly save and return ID")
    void save_shouldPersistUser() {
        // given
        UserInfo user = new UserInfo();
        user.setUsername("new_user");
        user.setPassword("pass");
        user.setRole(Role.ADMIN);

        // when
        UserInfo savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.findById(savedUser.getId())).isPresent();
    }
}

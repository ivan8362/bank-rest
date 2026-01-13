package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CardRepositoryPostgresTest {

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
    CardRepository cardRepository;

    @Autowired
    UserInfoRepository userRepository;

    @Test
    void saveAndFindById() {
        UserInfo user = userRepository.save(testUser("ivan"));

        Card card = cardRepository.save(testCard(user));

        Card found = cardRepository.findById(card.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getOwner().getId()).isEqualTo(user.getId());
        assertThat(found.getLast4()).isEqualTo("1234");
    }

    @Test
    void findAllByOwner_returnsOnlyOwnersCards() {
        UserInfo user1 = userRepository.save(testUser("ivan"));
        UserInfo user2 = userRepository.save(testUser("alex"));

        Card card1 = cardRepository.save(testCard(user1));
        Card card2 = cardRepository.save(testCard(user1));
        cardRepository.save(testCard(user2));

        List<Card> cards = cardRepository.findAllByOwner(user1);

        assertThat(cards)
            .hasSize(2)
            .extracting(Card::getId)
            .containsExactlyInAnyOrder(card1.getId(), card2.getId());
    }

    @Test
    void findAllByOwner_whenNoCards_returnsEmptyList() {
        UserInfo user = userRepository.save(testUser("no_cards"));

        List<Card> cards = cardRepository.findAllByOwner(user);

        assertThat(cards).isEmpty();
    }

    // ---------- helpers ----------

    private UserInfo testUser(String username) {
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword("password");
        user.setRole(Role.USER);
        return user;
    }

    private Card testCard(UserInfo owner) {
        Card card = new Card();
        card.setOwner(owner);
        card.setCardNumberEncrypted("encrypted");
        card.setLast4("1234");
        card.setExpiryDate(YearMonth.of(2026, Month.JANUARY));
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));
        return card;
    }
}

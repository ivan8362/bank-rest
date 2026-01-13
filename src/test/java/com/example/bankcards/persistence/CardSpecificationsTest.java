package com.example.bankcards.persistence;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserInfoRepository;
import com.example.bankcards.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CardSpecificationsTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserInfoRepository userRepository;

    private UserInfo testUser;
    private UserInfo otherUser;

    @BeforeEach
    void setUp() {
        testUser = new UserInfo();
        testUser.setUsername("owner");
        testUser.setPassword("pass");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        otherUser = new UserInfo();
        otherUser.setUsername("other");
        otherUser.setPassword("pass");
        otherUser.setRole(Role.USER);
        userRepository.save(otherUser);

        // Создаем набор тестовых карт
        cardRepository.save(createCard("1111", testUser, Status.ACTIVE, new BigDecimal("100.00"), YearMonth.of(2025, 12)));
        cardRepository.save(createCard("2222", testUser, Status.BLOCKED, new BigDecimal("500.00"), YearMonth.of(2024, 1)));
        cardRepository.save(createCard("3333", otherUser, Status.ACTIVE, new BigDecimal("1000.00"), YearMonth.of(2026, 6)));
    }

    @Test
    @DisplayName("Filter cards by owner")
    void belongsTo_shouldReturnUserCards() {
        List<Card> cards = cardRepository.findAll(CardSpecifications.belongsTo(testUser));
        assertThat(cards).hasSize(2).allMatch(c -> c.getOwner().equals(testUser));
    }

    @Test
    @DisplayName("Filter cards by status")
    void hasStatus_shouldReturnBlockedCards() {
        List<Card> cards = cardRepository.findAll(CardSpecifications.hasStatus(Status.BLOCKED));
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getLast4()).isEqualTo("2222");
    }

    @Test
    @DisplayName("Filter cards with balance in range")
    void balanceRange_shouldReturnMatchingCards() {
        Specification<Card> spec = CardSpecifications.balanceMin(new BigDecimal("200"))
            .and(CardSpecifications.balanceMax(new BigDecimal("600")));

        List<Card> cards = cardRepository.findAll(spec);
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Filter cards by expiry date (before)")
    void expiryBefore_shouldReturnExpiredCards() {
        YearMonth date = YearMonth.of(2024, 5);
        List<Card> cards = cardRepository.findAll(CardSpecifications.expiryBefore(date));

        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getLast4()).isEqualTo("2222");
    }

    @Test
    @DisplayName("Combine multiple specifications")
    void combinedSpecs_shouldWork() {
        Specification<Card> spec = CardSpecifications.belongsTo(testUser)
            .and(CardSpecifications.hasStatus(Status.ACTIVE))
            .and(CardSpecifications.balanceMin(new BigDecimal("50")));

        List<Card> cards = cardRepository.findAll(spec);
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getLast4()).isEqualTo("1111");
    }

    private Card createCard(String number, UserInfo owner, Status status, BigDecimal balance, YearMonth expiry) {
        Card card = new Card();
        card.setCardNumberEncrypted("encrypted");
        card.setLast4(number);
        card.setOwner(owner);
        card.setStatus(status);
        card.setBalance(balance);
        card.setExpiryDate(expiry);
        return card;
    }
}

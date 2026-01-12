package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.security.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.liquibase.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Использовать in-memory БД
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserInfoRepository userRepository;

    @Test
    void findAllByOwner_shouldReturnOnlyUsersCards() {
        // given
        UserInfo user1 = new UserInfo();
        user1.setUsername("user1");
        user1.setPassword("pass12");
        user1.setRole(Role.USER);
        user1 = userRepository.save(user1);

        UserInfo user2 = new UserInfo();
        user2.setUsername("user2");
        user2.setPassword("pass12");
        user2.setRole(Role.USER);
        user2 = userRepository.save(user2);

        Card card1 = buildCard(user1, "1111");
        Card card2 = buildCard(user1, "2222");
        Card card3 = buildCard(user2, "3333");

        cardRepository.save(card1);
        cardRepository.save(card2);
        cardRepository.save(card3);

        // when
        List<Card> result = cardRepository.findAllByOwner(user1);

        // then
        assertThat(result).hasSize(2);
        for (Card card: result) {
            Assertions.assertEquals(card.getOwner().getId(), user1.getId());
        }
    }

    @Test
    void findAllByOwner_shouldReturnEmptyList_whenUserHasNoCards() {
        UserInfo user = new UserInfo();
        user.setUsername("lonely");
        user.setPassword("pass12");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        List<Card> cards = cardRepository.findAllByOwner(user);

        assertThat(cards).isEmpty();
    }

    @Test
    void saveAndFindById_shouldWorkCorrectly() {
        UserInfo user = new UserInfo();
        user.setUsername("ivan");
        user.setPassword("pass12");
        user.setRole(Role.USER);
        user = userRepository.save(user);

        Card card = buildCard(user, "9999");
        Card saved = cardRepository.save(card);

        Card found = cardRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getLast4()).isEqualTo("9999");
        assertThat(found.getOwner().getId()).isEqualTo(user.getId());
    }

    private Card buildCard(UserInfo owner, String last4) {
        Card card = new Card();
        card.setOwner(owner);
        card.setLast4(last4);
        card.setCardNumberEncrypted("encrypted");
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setExpiryDate(YearMonth.of(2030, 12));
        return card;
    }
}

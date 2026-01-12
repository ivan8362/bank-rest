package com.example.bankcards.service;

import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.dto.EditCardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserInfoRepository;
import com.example.bankcards.security.CardCryptoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    CardRepository cardRepository;

    @Mock
    UserInfoRepository userRepository;

    @Mock
    CardCryptoService cardCryptoService;

    @InjectMocks
    CardService cardService;

    // ---------- getMyCardsNoSearch ----------

    @Test
    void getMyCardsNoSearch_shouldReturnMaskedCards() {
        UserInfo user = new UserInfo();
        user.setId(1L);

        Card card = new Card();
        card.setId(10L);
        card.setOwner(user);
        card.setLast4("1234");
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.TEN);
        card.setExpiryDate(YearMonth.of(2030, 12));

        when(cardRepository.findAllByOwner(user))
            .thenReturn(List.of(card));

        var result = cardService.getMyCardsNoSearch(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).maskedNumber())
            .isEqualTo("**** **** **** 1234");
    }

    // ---------- createCard ----------

    @Test
    void createCard_shouldEncryptAndSaveCard() {
        CreateCardDto dto = new CreateCardDto(
            "1111222233334444",
            1L,
            YearMonth.of(2030, 12)
        );

        UserInfo owner = new UserInfo();
        owner.setId(1L);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(owner));
        when(cardCryptoService.encrypt("1111222233334444"))
            .thenReturn("encrypted");
        when(cardCryptoService.extractLast4("1111222233334444"))
            .thenReturn("4444");
        when(cardRepository.save(any(Card.class)))
            .thenAnswer(inv -> {
                Card c = inv.getArgument(0);
                c.setId(99L);
                return c;
            });

        Long id = cardService.createCard(dto);

        assertThat(id).isEqualTo(99L);

        verify(cardCryptoService).encrypt(anyString());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_shouldFail_whenUserNotFound() {
        CreateCardDto dto = new CreateCardDto(
            "1111",
            1L,
            YearMonth.now()
        );

        when(userRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(dto))
            .isInstanceOf(EntityNotFoundException.class);
    }

    // ---------- editCard ----------

    @Test
    void editCard_shouldUpdateStatus() {
        Card card = new Card();
        card.setId(1L);
        card.setStatus(Status.ACTIVE);

        EditCardDto dto = new EditCardDto(1L, Status.BLOCKED);

        when(cardRepository.findById(1L))
            .thenReturn(Optional.of(card));

        cardService.editCard(dto);

        assertThat(card.getStatus()).isEqualTo(Status.BLOCKED);
        verify(cardRepository).saveAndFlush(card);
    }

    @Test
    void editCard_shouldFail_whenStatusExpired() {
        EditCardDto dto = new EditCardDto(1L, Status.EXPIRED);

        assertThatThrownBy(() -> cardService.editCard(dto))
            .isInstanceOf(IllegalStateException.class);
    }

    // ---------- transferMoney ----------

    @Test
    void transferMoney_shouldTransferBetweenCards() {
        UserInfo user = new UserInfo();
        user.setId(1L);

        Card from = new Card();
        from.setId(1L);
        from.setOwner(user);
        from.setStatus(Status.ACTIVE);
        from.setBalance(BigDecimal.valueOf(100));

        Card to = new Card();
        to.setId(2L);
        to.setOwner(user);
        to.setStatus(Status.ACTIVE);
        to.setBalance(BigDecimal.valueOf(50));

        TransferRequest request =
            new TransferRequest(1L, 2L, BigDecimal.valueOf(30));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        cardService.transferMoney(request, user);

        assertThat(from.getBalance()).isEqualTo(BigDecimal.valueOf(70));
        assertThat(to.getBalance()).isEqualTo(BigDecimal.valueOf(80));

        verify(cardRepository).save(from);
        verify(cardRepository).save(to);
    }

    @Test
    void transferMoney_shouldFail_whenInsufficientFunds() {
        UserInfo user = new UserInfo();
        user.setId(1L);

        Card from = new Card();
        from.setOwner(user);
        from.setStatus(Status.ACTIVE);
        from.setBalance(BigDecimal.TEN);

        Card to = new Card();
        to.setOwner(user);
        to.setStatus(Status.ACTIVE);

        TransferRequest request =
            new TransferRequest(1L, 2L, BigDecimal.valueOf(100));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> cardService.transferMoney(request, user))
            .isInstanceOf(IllegalStateException.class);
    }

    // ---------- getAmount ----------

    @Test
    void getAmount_shouldReturnBalance_forOwner() {
        UserInfo user = new UserInfo();
        user.setId(1L);

        Card card = new Card();
        card.setOwner(user);
        card.setBalance(BigDecimal.valueOf(42));

        when(cardRepository.findById(1L))
            .thenReturn(Optional.of(card));

        BigDecimal amount = cardService.getAmount(1L, user);

        assertThat(amount).isEqualTo(BigDecimal.valueOf(42));
    }

    @Test
    void getAmount_shouldFail_forAnotherUser() {
        UserInfo owner = new UserInfo();
        owner.setId(1L);

        UserInfo attacker = new UserInfo();
        attacker.setId(2L);

        Card card = new Card();
        card.setOwner(owner);

        when(cardRepository.findById(1L))
            .thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.getAmount(1L, attacker))
            .isInstanceOf(AccessDeniedException.class);
    }

    // ---------- requestBlock ----------

    @Test
    void requestBlock_shouldBlockCard() {
        UserInfo user = new UserInfo();
        user.setId(1L);

        Card card = new Card();
        card.setOwner(user);
        card.setStatus(Status.ACTIVE);

        when(cardRepository.findById(1L))
            .thenReturn(Optional.of(card));

        cardService.requestBlock(1L, user);

        assertThat(card.getStatus()).isEqualTo(Status.BLOCKED);
        verify(cardRepository).save(card);
    }
}

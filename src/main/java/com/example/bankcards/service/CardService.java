package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CardCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardCryptoService cardCryptoService;

    public List<CardDto> getMyCards(UserInfo user) {
        return cardRepository.findByOwner(user).stream()
            .map(card -> new CardDto(
                card.getId(),
//                maskCardNumber(card.getCardNumberEncrypted()),
                maskCardNumber(cardCryptoService.decrypt(card.getCardNumberEncrypted())),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance()
            ))
            .toList();
    }

    private String maskCardNumber(String plainCardNumber) {
        String last4 = plainCardNumber.substring(plainCardNumber.length() - 4);
        return "**** **** **** " + last4;
    }

    public Card createCard(String cardNumber, UserInfo owner) {

        String encrypted = cardCryptoService.encrypt(cardNumber);
        String last4 = cardCryptoService.extractLast4(cardNumber);

        Card card = new Card();
        card.setCardNumberEncrypted(encrypted);
        card.setLast4(last4);
        card.setOwner(owner);
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card);
    }

}

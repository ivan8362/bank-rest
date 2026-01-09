package com.example.bankcards.util;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

//@Component
public class CardMapper {

    private CardMapper() {
    }

    public static CardDto toDto(Card card) {
        return new CardDto(card.getId(), "**** **** **** " + card.getLast4(),
            card.getOwner().getId(),
            card.getExpiryDate(), card.getStatus(), card.getBalance());
    }

//    public Card toEntity(CardDto cardDto) {
//    }
}

package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.dto.EditCardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.entity.Card;
import com.example.bankcards.persistence.CardSpecifications;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserInfoRepository;
import com.example.bankcards.security.CardCryptoService;
import com.example.bankcards.util.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardCryptoService cardCryptoService;
    private final UserInfoRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(CardService.class);

    public List<CardDto> getMyCards1(UserInfo user) {
        return cardRepository.findByOwner(user).stream()
            .map(card -> new CardDto(
                card.getId(),
//                maskCardNumber(card.getCardNumberEncrypted()),
                maskCardNumber(cardCryptoService.decrypt(card.getCardNumberEncrypted())),
                card.getOwner().getId(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance()
            ))
            .toList();
    }

    public Page<CardDto> getMyCards(
        UserInfo user,
        Status status,
        YearMonth expiryBefore,
        YearMonth expiryAfter,
        BigDecimal balanceMin,
        BigDecimal balanceMax,
        Pageable pageable
    ) {
        Specification<Card> spec = Specification
            .where(CardSpecifications.belongsTo(user));

        if (status != null) {
            spec = spec.and(CardSpecifications.hasStatus(status));
        }
        if (expiryBefore != null) {
            spec = spec.and(CardSpecifications.expiryBefore(expiryBefore));
        }
        if (expiryAfter != null) {
            spec = spec.and(CardSpecifications.expiryAfter(expiryAfter));
        }
        if (balanceMin != null) {
            spec = spec.and(CardSpecifications.balanceMin(balanceMin));
        }
        if (balanceMax != null) {
            spec = spec.and(CardSpecifications.balanceMax(balanceMax));
        }

        return cardRepository.findAll(spec, pageable)
            .map(CardMapper::toDto);
    }

    private String maskCardNumber(String plainCardNumber) {
        String last4 = plainCardNumber.substring(plainCardNumber.length() - 4);
        return "**** **** **** " + last4;
    }

    public Long createCard(final CreateCardDto createDto) {
        String cardNumber = createDto.getCardNumber();
        UserInfo owner = userRepository.findById(createDto.getUserId())
            .orElseThrow(() -> new RuntimeException(String
                .format("User with id: %d not found", createDto.getUserId())));

        String encrypted = cardCryptoService.encrypt(cardNumber);
        String last4 = cardCryptoService.extractLast4(cardNumber);

        Card card = new Card();
        card.setCardNumberEncrypted(encrypted);
        card.setLast4(last4);
        card.setOwner(owner);
        card.setExpiryDate(createDto.getExpiryDate());
        card.setStatus(Status.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card).getId();
    }

    public List<CardDto> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        LOGGER.info("Received all cards from DB. Quantity: {}", cards.size());
        return cards.stream().map(CardMapper::toDto).toList();
    }

    public void editCard(EditCardDto editCardDto) {
        if (editCardDto.getStatus().equals(Status.EXPIRED)) {
            throw new RuntimeException("Called API edit-card with a wrong card status.");
        }
        Card card = cardRepository.findById(editCardDto.getCardId())
            .orElseThrow(() -> new EntityNotFoundException("Card not found: " + editCardDto.getCardId()));
        card.setStatus(editCardDto.getStatus());
        cardRepository.saveAndFlush(card);
        LOGGER.info("Successfully updated card's status. Card id: {}", card.getId());
    }

    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
        LOGGER.info("Successfully deleted card with id: {}", cardId);
    }

//    public void transferMoney1(TransferRequest request) {
//        Card cardFrom = cardRepository.getReferenceById(request.getFromCard());
//        if (cardFrom.getBalance().compareTo(request.getAmount()) < 0)  {
//            LOGGER.error("Card with id: {} has insufficient funds for the transfer", cardFrom.getId());
//            throw new RuntimeException(String
//                .format("Card with id: %d has insufficient funds for the transfer", cardFrom.getId()));
//        }
//    }

    @Transactional
    public void transferMoney(TransferRequest request, UserInfo user) {
        if (request.getFromCard().equals(request.getToCard())) {
            throw new IllegalArgumentException("Source and target cards must be different");
        }

        Card cardFrom = cardRepository.findById(request.getFromCard())
            .orElseThrow(() -> new EntityNotFoundException("Card not found: " + request.getFromCard()));
        Card cardTo = cardRepository.findById(request.getToCard())
            .orElseThrow(() -> new EntityNotFoundException("Card not found: " + request.getToCard()));;

        if (!cardFrom.getOwner().equals(user) || !cardTo.getOwner().equals(user)) {
            throw new AccessDeniedException("Card does not belong to user");
        }
        if (cardFrom.getStatus() != Status.ACTIVE || cardTo.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }
        if (cardFrom.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException(
                "Insufficient funds on card " + cardFrom.getId()
            );
        }

        cardFrom.setBalance(cardFrom.getBalance().subtract(request.getAmount()));
        cardTo.setBalance(cardTo.getBalance().add(request.getAmount()));

        cardRepository.save(cardFrom);
        cardRepository.save(cardTo);
    }

    public BigDecimal getAmount(final Long cardId, UserInfo user) {
        Card card = cardRepository.findById(cardId)
            .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardId));;
        if (!card.getOwner().equals(user)) {
            throw new AccessDeniedException("Card does not belong to user");
        }
        return card.getBalance();
    }
/*
    public void updateCard1(CardDto cardDto) {
//        if (editCardDto.getStatus().equals(Status.EXPIRED)) {
//            throw new RuntimeException("Called API edit-card with a wrong card status.");
//        }
        Card card = cardRepository.findById(cardDto.id())
            .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardDto.id()));;
        card.setStatus(cardDto.getStatus());
        cardRepository.saveAndFlush(card);
        LOGGER.info("Successfully updated card's status. Card id: {}", card.getId());
    }
 */

    @Transactional
    public void updateCard(CardDto dto) {

        Card card = cardRepository.findById(dto.id())
            .orElseThrow(() ->
                new EntityNotFoundException("Card not found: " + dto.id()));

        // owner
        UserInfo owner = userRepository.findById(dto.owner())
            .orElseThrow(() ->
                new EntityNotFoundException("User not found: " + dto.owner()));
        card.setOwner(owner);

        // expiry date
        card.setExpiryDate(dto.expiryDate());

        // status
        card.setStatus(dto.status());

        // balance. Такого в prod никогда не будет, но я написал, так как в ТЗ ничего не написано.
        card.setBalance(dto.balance());

        cardRepository.save(card);

        LOGGER.info("Card {} updated by ADMIN", card.getId());
    }
}

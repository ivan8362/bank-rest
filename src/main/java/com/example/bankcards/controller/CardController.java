package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.dto.EditCardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CardController.class);

    @GetMapping("/hello")
    public ResponseEntity<?> getById() {
        return ResponseEntity.ok("hello-world");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Long createCard(@Valid @RequestBody CreateCardDto createCardDto) {
        LOGGER.info("Called API POST /cards/new");
        return cardService.createCard(createCardDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cards")
    public List<CardDto> getAllCards() {
        LOGGER.info("Called API GET /cards/all");
        return cardService.getAllCards();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/status")
    public void editCardStatus(@Valid @RequestBody EditCardDto editCardDto) {
        LOGGER.info("Called API PATCH /cards/status");
        cardService.editCard(editCardDto);
    }

    /**
     * Понятно, что админ никогда не будет изменять баланс карты
     * пользователя. Но в ТЗ написано "CRUD для карт", поэтому
     * делаю, как написано.
     * @param cardDto банковская карта.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public void updateCard(@Valid @RequestBody CardDto cardDto) {
        LOGGER.info("Called API PATCH /cards");
        cardService.updateCard(cardDto);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cardId}")
    public void deleteCard(@PathVariable Long cardId) {
        LOGGER.info("Called API DELETE /cards/{id}");
        cardService.deleteCard(cardId);
    }

    @GetMapping("/simple")
    @PreAuthorize("hasRole('USER')")
    public List<CardDto> myCardsNoSearch(Authentication authentication) {
        UserInfo user = (UserInfo) authentication.getPrincipal();
        return cardService.getMyCardsNoSearch(user);
    }

    @GetMapping("my")
    @PreAuthorize("hasRole('USER')")
    public Page<CardDto> myCards(
        Authentication authentication,
        @RequestParam(required = false) Status status,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth expiryBefore,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth expiryAfter,
        @RequestParam(required = false) BigDecimal balanceMin,
        @RequestParam(required = false) BigDecimal balanceMax,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        LOGGER.info("Called API GET /cards/my");

        UserInfo user = (UserInfo) authentication.getPrincipal();
        return cardService.getMyCards(
            user,
            status,
            expiryBefore,
            expiryAfter,
            balanceMin,
            balanceMax,
            PageRequest.of(page, size)
        );
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> transferMoney(@Valid @RequestBody TransferRequest request,
                                                Authentication authentication) {
        LOGGER.info("Called API POST /cards/transfer");
        UserInfo user = (UserInfo) authentication.getPrincipal();
        cardService.transferMoney(request, user);
        return ResponseEntity.ok("Transfer successful");
    }

    @GetMapping("/balance/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long cardId,
                                                Authentication authentication) {
        LOGGER.info("Called API GET /cards/balance/{cardId}");
        UserInfo user = (UserInfo) authentication.getPrincipal();
        BigDecimal amount = cardService.getAmount(cardId, user);
        return ResponseEntity.ok(amount);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{cardId}/block")
    public void requestBlock(@PathVariable Long cardId, Authentication auth) {
        UserInfo user = (UserInfo) auth.getPrincipal();
        cardService.requestBlock(cardId, user);
    }
}

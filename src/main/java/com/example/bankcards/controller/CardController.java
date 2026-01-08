package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Status;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/hello")
    public ResponseEntity<?> getById() {
        return ResponseEntity.ok("hello-world");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cards")
    public void createCard(@RequestBody CreateCardDto createCardDto) {
        cardService.createCard(createCardDto.getCardNumber(), createCardDto.getUser());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<CardDto> myCards() {
        return List.of(new CardDto(2L, "ABCD", YearMonth.of(2022, 1),
            Status.ACTIVE, BigDecimal.ONE));
    }

//    @GetMapping("/my1")
//    @PreAuthorize("hasRole('USER')")
//    public List<CardDto> myCards(Authentication authentication) {
//        UserInfo user = (UserInfo) authentication.getPrincipal();
//        return cardService.getMyCards(user);
//    }

}

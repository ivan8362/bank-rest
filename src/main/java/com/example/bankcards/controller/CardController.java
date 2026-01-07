package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("cards")
public class CardController {

    @GetMapping("/hello")
    public ResponseEntity<?> getById() {
        return ResponseEntity.ok("hello-world");
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cards")
    public void createCard() {}

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cards/my")
    public List<CardDto> myCards() {
        return List.of(new CardDto(1, "john", YearMonth.of(2022, 1),
            Status.ACTIVE, BigDecimal.ONE));
    }

}

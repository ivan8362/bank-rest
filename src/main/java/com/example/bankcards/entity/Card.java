package com.example.bankcards.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
public class Card {
    private long number;
    private String owner;
    private YearMonth expireDate;
    private Status status;
    private BigDecimal balance;
}
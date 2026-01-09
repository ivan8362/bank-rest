package com.example.bankcards.dto;

import com.example.bankcards.entity.Status;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CardDto (@NotNull Long id,
                       String maskedNumber,
                       Long owner,
                       YearMonth expiryDate,
                       Status status,
                       BigDecimal balance) {}

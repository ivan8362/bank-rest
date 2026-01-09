package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransferRequest {

    @NotNull(message = "from-account must not be null")
    private Long fromCard;

    @NotNull(message = "to-account must not be null")
    private Long toCard;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 20, fraction = 2)
    private BigDecimal amount;
}

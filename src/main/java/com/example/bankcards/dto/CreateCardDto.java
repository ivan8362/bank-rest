package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.YearMonth;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCardDto {

    @NotBlank(message = "cardNumber must not be blank")
    @Pattern(regexp = "\\d{16}")
    private String cardNumber;

    @NotNull(message = "userId must not be null")
    private Long userId;

    @NotNull(message = "expiryDate must not be null")
    private YearMonth expiryDate;
}

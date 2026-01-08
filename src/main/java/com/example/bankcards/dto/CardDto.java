package com.example.bankcards.dto;

import com.example.bankcards.entity.Status;

import java.math.BigDecimal;
import java.time.YearMonth;

//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
public record CardDto (Long id,
                       String maskedNumber,
                       YearMonth expiryDate,
                       Status status,
                       BigDecimal balance) {
//    private long number;
//    private long owner;
//    private YearMonth expireDate;
//    private Status status;
//    private BigDecimal balance;
}

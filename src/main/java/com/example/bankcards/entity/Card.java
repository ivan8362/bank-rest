package com.example.bankcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "number", nullable = false)
    private Long number;
    @Column(name = "owner")
    private String owner;

    /** Срок действия карты (месяц/год), храним как первый день месяца */
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    @Column(name = "status", nullable = false)
    private Status status;
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
}
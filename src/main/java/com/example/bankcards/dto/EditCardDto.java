package com.example.bankcards.dto;

import com.example.bankcards.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EditCardDto {

    @NotNull(message = "cardId must not be null")
    private Long cardId;

    @NotNull(message = "status must not be null")
    private Status status;
}

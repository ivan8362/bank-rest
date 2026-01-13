package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardDto;
import com.example.bankcards.dto.EditCardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CardController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    /* ---------------- ADMIN endpoints ---------------- */

    @Test
    void createCard_ok() throws Exception {
        CreateCardDto dto = new CreateCardDto();
        dto.setCardNumber("1111222233334444");
        dto.setUserId(1L);
        dto.setExpiryDate(YearMonth.of(2030, Month.JANUARY));

        when(cardService.createCard(any())).thenReturn(42L);

        mockMvc.perform(post("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(content().string("42"));

        verify(cardService).createCard(any(CreateCardDto.class));
    }

    @Test
    void getAllCards_ok() throws Exception {
        when(cardService.getAllCards()).thenReturn(List.of());

        mockMvc.perform(get("/cards/cards"))
            .andExpect(status().isOk());

        verify(cardService).getAllCards();
    }

    @Test
    void editCardStatus_ok() throws Exception {
        EditCardDto dto = new EditCardDto();
        dto.setCardId(1L);
        dto.setStatus(Status.BLOCKED);

        mockMvc.perform(patch("/cards/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());

        verify(cardService).editCard(any(EditCardDto.class));
    }

    @Test
    void updateCard_ok() throws Exception {
        CardDto dto = new CardDto(1L, "1111", 10L,
            YearMonth.of(2030, Month.JANUARY), Status.ACTIVE, BigDecimal.TEN);

        mockMvc.perform(patch("/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());

        verify(cardService).updateCard(any(CardDto.class));
    }

    @Test
    void deleteCard_ok() throws Exception {
        mockMvc.perform(delete("/cards/{id}", 10L))
            .andExpect(status().isOk());

        verify(cardService).deleteCard(10L);
    }

    /* ---------------- USER endpoints ---------------- */

    @Test
    void myCardsNoSearch_ok() throws Exception {
        UserInfo user = new UserInfo();
        when(cardService.getMyCardsNoSearch(any())).thenReturn(List.of());

        mockMvc.perform(get("/cards/simple")
                .principal(new UsernamePasswordAuthenticationToken(user, "password")))
            .andExpect(status().isOk());

        verify(cardService).getMyCardsNoSearch(any(UserInfo.class));
    }

    @Test
    void myCards_withParams_ok() throws Exception {
        UserInfo user = new UserInfo();
        when(cardService.getMyCards(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(PageRequest.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/cards")
                .param("page", "0")
                .param("size", "5")
                .principal(new UsernamePasswordAuthenticationToken(user, "password")))
            .andExpect(status().isOk());

        verify(cardService).getMyCards(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(PageRequest.class)
        );
    }

    @Test
    void transferMoney_ok() throws Exception {
        UserInfo user = new UserInfo();
        TransferRequest request = new TransferRequest();
        request.setFromCard(1L);
        request.setToCard(2L);
        request.setAmount(BigDecimal.TEN);

        mockMvc.perform(post("/cards/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(new UsernamePasswordAuthenticationToken(user, "password")))
            .andExpect(status().isOk())
            .andExpect(content().string("Transfer successful"));

        verify(cardService).transferMoney(any(), any(UserInfo.class));
    }

    @Test
    void getBalance_ok() throws Exception {
        UserInfo user = new UserInfo();
        when(cardService.getAmount(eq(1L), any()))
            .thenReturn(new BigDecimal("50"));

        mockMvc.perform(get("/cards/balance/{id}", 1L)
                .principal(new UsernamePasswordAuthenticationToken(user, "password")))
            .andExpect(status().isOk())
            .andExpect(content().string("50"));

        verify(cardService).getAmount(eq(1L), any(UserInfo.class));
    }

    @Test
    void requestBlock_ok() throws Exception {
        UserInfo user = new UserInfo();
        mockMvc.perform(post("/cards/{id}/block", 5L)
                .principal(new UsernamePasswordAuthenticationToken(user, "password")))
            .andExpect(status().isOk());

        verify(cardService).requestBlock(eq(5L), any(UserInfo.class));
    }
}

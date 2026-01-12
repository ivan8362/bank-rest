package com.example.bankcards.security;

public interface CardCryptoService {

    String encrypt(String plainCardNumber);

    String decrypt(String encryptedCardNumber);

    String extractLast4(String plainCardNumber);
}

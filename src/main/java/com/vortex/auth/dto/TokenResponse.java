package com.vortex.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tipo,
    long accessTokenExpiraEmSegundos,
    long refreshTokenExpiraEmSegundos,
    boolean deveDefinirSenha) {}

package com.vortex.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record TokenResponse(
    String accessToken,
    @JsonIgnore String refreshToken,
    String tipo,
    long accessTokenExpiraEmSegundos,
    long refreshTokenExpiraEmSegundos,
    boolean deveDefinirSenha) {}

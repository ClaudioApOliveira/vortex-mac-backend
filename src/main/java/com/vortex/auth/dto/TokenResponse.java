package com.vortex.auth.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TokenResponse(
    String accessToken,
    String tipo,
    long accessTokenExpiraEmSegundos,
    long refreshTokenExpiraEmSegundos,
    boolean deveDefinirSenha) {}

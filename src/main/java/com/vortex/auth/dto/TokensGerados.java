package com.vortex.auth.dto;

public record TokensGerados(
    String accessToken,
    String refreshToken,
    String tipo,
    long accessTokenExpiraEmSegundos,
    long refreshTokenExpiraEmSegundos,
    boolean deveDefinirSenha) {

  public TokenResponse toResponse() {
    return new TokenResponse(
        accessToken,
        tipo,
        accessTokenExpiraEmSegundos,
        refreshTokenExpiraEmSegundos,
        deveDefinirSenha);
  }
}

package com.vortex.auth.security;

public interface JwtService {

  AccessTokenGerado gerarToken(
      Long usuarioId, String email, String nome, String perfil, Long clienteId);

  long getAccessTokenExpiraEmSegundos();
}

package com.vortex.auth.security;

import com.vortex.auth.entity.RefreshToken;

public interface RefreshTokenService {

  String criar(Long usuarioId);

  RefreshToken validar(String refreshToken);

  void revogar(String refreshToken);

  void revogarTodosPorUsuario(Long usuarioId);

  long getRefreshTokenExpiraEmSegundos();
}

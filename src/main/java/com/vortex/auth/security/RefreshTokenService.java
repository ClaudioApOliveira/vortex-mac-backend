package com.vortex.auth.security;

import com.vortex.auth.entity.RefreshToken;
import com.vortex.usuario.entity.Usuario;

public interface RefreshTokenService {

  String criar(Usuario usuario);

  RefreshToken validarERevogar(String refreshToken);

  void revogar(String refreshToken);

  void revogarTodosPorUsuario(Long usuarioId);

  long getRefreshTokenExpiraEmSegundos();
}

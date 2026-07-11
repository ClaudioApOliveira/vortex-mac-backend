package com.vortex.auth.security;

public interface SessaoService {

  void registrarAccess(String jti, Long usuarioId, long ttlSegundos);

  void registrarRefresh(String refreshToken, Long usuarioId, long ttlSegundos);

  boolean accessAtivo(String jti);

  boolean refreshAtivo(String refreshToken);

  void revogarAccess(String jti);

  void revogarRefresh(String refreshToken);

  void invalidarRefreshPorUsuario(Long usuarioId, long ttlSegundos);

  boolean refreshInvalidadoPorUsuario(Long usuarioId);

  void liberarRefreshPorUsuario(Long usuarioId);
}

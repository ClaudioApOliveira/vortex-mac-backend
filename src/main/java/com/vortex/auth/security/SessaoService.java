package com.vortex.auth.security;

/**
 * Allowlist de access tokens ativos, consultada a cada requisição autenticada. A validade dos
 * refresh tokens é controlada exclusivamente pela tabela {@code refresh_tokens}.
 */
public interface SessaoService {

  void registrarAccess(String jti, Long usuarioId, long ttlSegundos);

  boolean accessAtivo(String jti);

  void revogarAccess(String jti);

  void invalidarAccessPorUsuario(Long usuarioId);
}

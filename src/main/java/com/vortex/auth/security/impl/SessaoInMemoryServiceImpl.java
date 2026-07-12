package com.vortex.auth.security.impl;

import com.vortex.auth.security.SessaoService;
import com.vortex.auth.security.TokenHashUtil;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Alternative
@Priority(1)
@IfBuildProfile("test")
public class SessaoInMemoryServiceImpl implements SessaoService {

  private final Set<String> accessTokens = ConcurrentHashMap.newKeySet();
  private final Set<String> refreshTokens = ConcurrentHashMap.newKeySet();
  private final Set<Long> usuariosRefreshRevogados = ConcurrentHashMap.newKeySet();

  @Override
  public void registrarAccess(String jti, Long usuarioId, long ttlSegundos) {
    accessTokens.add(jti);
  }

  @Override
  public void registrarRefresh(String refreshToken, Long usuarioId, long ttlSegundos) {
    refreshTokens.add(TokenHashUtil.hash(refreshToken));
  }

  @Override
  public boolean accessAtivo(String jti) {
    return accessTokens.contains(jti);
  }

  @Override
  public boolean refreshAtivo(String refreshToken) {
    return refreshTokens.contains(TokenHashUtil.hash(refreshToken));
  }

  @Override
  public void revogarAccess(String jti) {
    accessTokens.remove(jti);
  }

  @Override
  public void revogarRefresh(String refreshToken) {
    refreshTokens.remove(TokenHashUtil.hash(refreshToken));
  }

  @Override
  public void invalidarRefreshPorUsuario(Long usuarioId, long ttlSegundos) {
    usuariosRefreshRevogados.add(usuarioId);
  }

  @Override
  public boolean refreshInvalidadoPorUsuario(Long usuarioId) {
    return usuariosRefreshRevogados.contains(usuarioId);
  }

  @Override
  public void liberarRefreshPorUsuario(Long usuarioId) {
    usuariosRefreshRevogados.remove(usuarioId);
  }
}

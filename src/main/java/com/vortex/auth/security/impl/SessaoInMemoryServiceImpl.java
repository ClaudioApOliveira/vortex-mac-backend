package com.vortex.auth.security.impl;

import com.vortex.auth.security.SessaoService;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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
    refreshTokens.add(hashToken(refreshToken));
  }

  @Override
  public boolean accessAtivo(String jti) {
    return accessTokens.contains(jti);
  }

  @Override
  public boolean refreshAtivo(String refreshToken) {
    return refreshTokens.contains(hashToken(refreshToken));
  }

  @Override
  public void revogarAccess(String jti) {
    accessTokens.remove(jti);
  }

  @Override
  public void revogarRefresh(String refreshToken) {
    refreshTokens.remove(hashToken(refreshToken));
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

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("Algoritmo SHA-256 não disponível", exception);
    }
  }
}

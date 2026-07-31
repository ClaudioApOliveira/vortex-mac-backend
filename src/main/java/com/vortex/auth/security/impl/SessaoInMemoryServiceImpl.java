package com.vortex.auth.security.impl;

import com.vortex.auth.security.SessaoService;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Alternative
@Priority(1)
@IfBuildProfile("test")
public class SessaoInMemoryServiceImpl implements SessaoService {

  private final Set<String> accessTokens = ConcurrentHashMap.newKeySet();
  private final Map<String, Long> accessPorJti = new ConcurrentHashMap<>();
  private final Map<Long, Set<String>> accessPorUsuario = new ConcurrentHashMap<>();

  @Override
  public void registrarAccess(String jti, Long usuarioId, long ttlSegundos) {
    accessTokens.add(jti);
    accessPorJti.put(jti, usuarioId);
    accessPorUsuario.computeIfAbsent(usuarioId, id -> ConcurrentHashMap.newKeySet()).add(jti);
  }

  @Override
  public boolean accessAtivo(String jti) {
    return accessTokens.contains(jti);
  }

  @Override
  public void revogarAccess(String jti) {
    accessTokens.remove(jti);
    Long usuarioId = accessPorJti.remove(jti);
    if (usuarioId != null) {
      Set<String> jtis = accessPorUsuario.get(usuarioId);
      if (jtis != null) {
        jtis.remove(jti);
      }
    }
  }

  @Override
  public void invalidarAccessPorUsuario(Long usuarioId) {
    Set<String> jtis = accessPorUsuario.remove(usuarioId);
    if (jtis == null) {
      return;
    }
    for (String jti : jtis) {
      accessTokens.remove(jti);
      accessPorJti.remove(jti);
    }
  }
}

package com.vortex.auth.security.impl;

import com.vortex.auth.security.SessaoService;
import com.vortex.auth.security.TokenHashUtil;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class SessaoValkeyServiceImpl implements SessaoService {

  private static final String PREFIXO_ACCESS = "mec:sessao:access:";
  private static final String PREFIXO_ACCESS_USUARIO = "mec:sessao:usuario:access:";
  private static final String PREFIXO_REFRESH = "mec:sessao:refresh:";
  private static final String PREFIXO_REFRESH_REVOGADO_USUARIO =
      "mec:sessao:usuario:refresh-revogado:";

  private final RedisDataSource redisDataSource;
  private ValueCommands<String, String> valores;
  private KeyCommands<String> chaves;
  private SetCommands<String, String> conjuntos;

  @Inject
  public SessaoValkeyServiceImpl(RedisDataSource redisDataSource) {
    this.redisDataSource = redisDataSource;
  }

  @PostConstruct
  void inicializar() {
    valores = redisDataSource.value(String.class);
    chaves = redisDataSource.key(String.class);
    conjuntos = redisDataSource.set(String.class);
  }

  @Override
  public void registrarAccess(String jti, Long usuarioId, long ttlSegundos) {
    String chave = PREFIXO_ACCESS + jti;
    valores.set(chave, String.valueOf(usuarioId));
    chaves.expire(chave, ttlSegundos);

    String chaveUsuario = PREFIXO_ACCESS_USUARIO + usuarioId;
    conjuntos.sadd(chaveUsuario, jti);
    chaves.expire(chaveUsuario, ttlSegundos);
  }

  @Override
  public void registrarRefresh(String refreshToken, Long usuarioId, long ttlSegundos) {
    String chave = PREFIXO_REFRESH + TokenHashUtil.hash(refreshToken);
    valores.set(chave, String.valueOf(usuarioId));
    chaves.expire(chave, ttlSegundos);
  }

  @Override
  public boolean accessAtivo(String jti) {
    return valores.get(PREFIXO_ACCESS + jti) != null;
  }

  @Override
  public boolean refreshAtivo(String refreshToken) {
    return valores.get(PREFIXO_REFRESH + TokenHashUtil.hash(refreshToken)) != null;
  }

  @Override
  public void revogarAccess(String jti) {
    String usuarioId = valores.get(PREFIXO_ACCESS + jti);
    chaves.del(PREFIXO_ACCESS + jti);
    if (usuarioId != null && !usuarioId.isBlank()) {
      conjuntos.srem(PREFIXO_ACCESS_USUARIO + usuarioId, jti);
    }
  }

  @Override
  public void revogarRefresh(String refreshToken) {
    chaves.del(PREFIXO_REFRESH + TokenHashUtil.hash(refreshToken));
  }

  @Override
  public void invalidarAccessPorUsuario(Long usuarioId) {
    String chaveUsuario = PREFIXO_ACCESS_USUARIO + usuarioId;
    Set<String> jtis = conjuntos.smembers(chaveUsuario);
    if (jtis != null) {
      for (String jti : jtis) {
        chaves.del(PREFIXO_ACCESS + jti);
      }
    }
    chaves.del(chaveUsuario);
  }

  @Override
  public void invalidarRefreshPorUsuario(Long usuarioId, long ttlSegundos) {
    String chave = PREFIXO_REFRESH_REVOGADO_USUARIO + usuarioId;
    valores.set(chave, "1");
    chaves.expire(chave, ttlSegundos);
  }

  @Override
  public boolean refreshInvalidadoPorUsuario(Long usuarioId) {
    return valores.get(PREFIXO_REFRESH_REVOGADO_USUARIO + usuarioId) != null;
  }

  @Override
  public void liberarRefreshPorUsuario(Long usuarioId) {
    chaves.del(PREFIXO_REFRESH_REVOGADO_USUARIO + usuarioId);
  }
}

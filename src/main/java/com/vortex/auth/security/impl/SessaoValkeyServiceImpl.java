package com.vortex.auth.security.impl;

import com.vortex.auth.security.SessaoService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@ApplicationScoped
public class SessaoValkeyServiceImpl implements SessaoService {

  private static final String PREFIXO_ACCESS = "mec:sessao:access:";
  private static final String PREFIXO_REFRESH = "mec:sessao:refresh:";
  private static final String PREFIXO_REFRESH_REVOGADO_USUARIO =
      "mec:sessao:usuario:refresh-revogado:";

  private final RedisDataSource redisDataSource;
  private ValueCommands<String, String> valores;
  private KeyCommands<String> chaves;

  @Inject
  public SessaoValkeyServiceImpl(RedisDataSource redisDataSource) {
    this.redisDataSource = redisDataSource;
  }

  @PostConstruct
  void inicializar() {
    valores = redisDataSource.value(String.class);
    chaves = redisDataSource.key(String.class);
  }

  @Override
  public void registrarAccess(String jti, Long usuarioId, long ttlSegundos) {
    String chave = PREFIXO_ACCESS + jti;
    valores.set(chave, String.valueOf(usuarioId));
    chaves.expire(chave, ttlSegundos);
  }

  @Override
  public void registrarRefresh(String refreshToken, Long usuarioId, long ttlSegundos) {
    String chave = PREFIXO_REFRESH + hashToken(refreshToken);
    valores.set(chave, String.valueOf(usuarioId));
    chaves.expire(chave, ttlSegundos);
  }

  @Override
  public boolean accessAtivo(String jti) {
    return valores.get(PREFIXO_ACCESS + jti) != null;
  }

  @Override
  public boolean refreshAtivo(String refreshToken) {
    return valores.get(PREFIXO_REFRESH + hashToken(refreshToken)) != null;
  }

  @Override
  public void revogarAccess(String jti) {
    chaves.del(PREFIXO_ACCESS + jti);
  }

  @Override
  public void revogarRefresh(String refreshToken) {
    chaves.del(PREFIXO_REFRESH + hashToken(refreshToken));
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

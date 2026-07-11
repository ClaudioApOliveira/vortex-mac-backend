package com.vortex.shared.ratelimit.impl;

import com.vortex.shared.ratelimit.RateLimitService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RateLimitValkeyServiceImpl implements RateLimitService {

  private final RedisDataSource redisDataSource;
  private ValueCommands<String, String> valores;
  private KeyCommands<String> chaves;

  @Inject
  public RateLimitValkeyServiceImpl(RedisDataSource redisDataSource) {
    this.redisDataSource = redisDataSource;
  }

  @PostConstruct
  void inicializar() {
    valores = redisDataSource.value(String.class);
    chaves = redisDataSource.key(String.class);
  }

  @Override
  public boolean tentarConsumir(String chave, int limite, long janelaSegundos) {
    long contagem = valores.incr(chave);
    if (contagem == 1) {
      chaves.expire(chave, janelaSegundos);
    }
    return contagem <= limite;
  }
}

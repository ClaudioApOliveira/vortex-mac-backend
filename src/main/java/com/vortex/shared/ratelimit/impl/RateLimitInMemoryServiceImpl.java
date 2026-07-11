package com.vortex.shared.ratelimit.impl;

import com.vortex.shared.ratelimit.RateLimitService;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
@Alternative
@Priority(1)
@IfBuildProfile("test")
public class RateLimitInMemoryServiceImpl implements RateLimitService {

  private final ConcurrentMap<String, Janela> janelas = new ConcurrentHashMap<>();

  @Override
  public boolean tentarConsumir(String chave, int limite, long janelaSegundos) {
    long agora = System.currentTimeMillis();
    long expiraEm = agora + janelaSegundos * 1000L;

    Janela janela =
        janelas.compute(
            chave,
            (key, atual) -> {
              if (atual == null || agora >= atual.expiraEm) {
                return new Janela(1, expiraEm);
              }
              return new Janela(atual.contagem + 1, atual.expiraEm);
            });

    return janela.contagem <= limite;
  }

  private record Janela(int contagem, long expiraEm) {}
}

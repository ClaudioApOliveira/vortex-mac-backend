package com.vortex.shared.ratelimit;

public interface RateLimitService {

  boolean tentarConsumir(String chave, int limite, long janelaSegundos);
}

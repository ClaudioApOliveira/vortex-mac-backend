package com.vortex.auth.service;

import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.ratelimit.RateLimitConfig;
import com.vortex.shared.ratelimit.RateLimitService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthEmailRateLimitService {

  private static final String PREFIXO_CHAVE = "mec:ratelimit:auth:email:";

  private final RateLimitService rateLimitService;
  private final RateLimitConfig rateLimitConfig;

  @Inject
  public AuthEmailRateLimitService(
      RateLimitService rateLimitService, RateLimitConfig rateLimitConfig) {
    this.rateLimitService = rateLimitService;
    this.rateLimitConfig = rateLimitConfig;
  }

  public void verificarLimite(String operacao, String email) {
    if (!rateLimitConfig.isEnabled()) {
      return;
    }

    String emailNormalizado = email.trim().toLowerCase();
    String chave = PREFIXO_CHAVE + operacao + ":" + emailNormalizado;
    boolean permitido =
        rateLimitService.tentarConsumir(
            chave, rateLimitConfig.getAuthEmailRequests(), rateLimitConfig.getAuthWindowSeconds());

    if (!permitido) {
      throw new BusinessException(
          "Muitas tentativas para este email. Tente novamente em alguns instantes.");
    }
  }
}

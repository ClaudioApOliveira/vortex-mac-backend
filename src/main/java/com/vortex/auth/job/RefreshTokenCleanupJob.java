package com.vortex.auth.job;

import com.vortex.auth.repository.RefreshTokenRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RefreshTokenCleanupJob {

  private static final Logger LOG = Logger.getLogger(RefreshTokenCleanupJob.class.getName());

  private final RefreshTokenRepository refreshTokenRepository;

  @Inject
  public RefreshTokenCleanupJob(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Scheduled(cron = "{vortex.jwt.refresh-token.cleanup-cron}")
  @Transactional
  void limparTokensExpirados() {
    int removidos = refreshTokenRepository.removerExpiradosERevogados();
    if (removidos > 0) {
      LOG.log(Level.FINE, "Removidos {0} refresh tokens expirados ou revogados", removidos);
    }
  }
}

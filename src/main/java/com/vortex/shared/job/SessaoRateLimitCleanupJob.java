package com.vortex.shared.job;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Remove linhas expiradas das tabelas UNLOGGED de sessão e rate limit. As leituras já filtram por
 * {@code expira_em > CURRENT_TIMESTAMP}, então a limpeza serve apenas para conter o crescimento das
 * tabelas. O DELETE é idempotente: com múltiplas instâncias, execuções concorrentes são
 * inofensivas.
 */
@ApplicationScoped
public class SessaoRateLimitCleanupJob {

  private static final Logger LOG = Logger.getLogger(SessaoRateLimitCleanupJob.class.getName());

  private static final String REMOVER_SESSOES_EXPIRADAS =
      "DELETE FROM sessoes_access WHERE expira_em < CURRENT_TIMESTAMP";

  private static final String REMOVER_RATE_LIMITS_EXPIRADOS =
      "DELETE FROM rate_limits WHERE expira_em < CURRENT_TIMESTAMP";

  @PersistenceContext EntityManager entityManager;

  @Scheduled(cron = "{vortex.sessao-rate-limit.cleanup-cron}")
  @Transactional
  void limparExpirados() {
    int sessoes = entityManager.createNativeQuery(REMOVER_SESSOES_EXPIRADAS).executeUpdate();
    int rateLimits = entityManager.createNativeQuery(REMOVER_RATE_LIMITS_EXPIRADOS).executeUpdate();
    if (sessoes > 0 || rateLimits > 0) {
      LOG.log(
          Level.FINE,
          "Removidos {0} sessões e {1} contadores de rate limit expirados",
          new Object[] {sessoes, rateLimits});
    }
  }
}

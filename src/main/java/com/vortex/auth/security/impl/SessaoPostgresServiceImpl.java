package com.vortex.auth.security.impl;

import com.vortex.auth.security.SessaoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Armazena as sessões de access token na tabela UNLOGGED {@code sessoes_access}: escrita barata
 * (sem WAL) e estado compartilhado entre instâncias do backend.
 */
@ApplicationScoped
public class SessaoPostgresServiceImpl implements SessaoService {

  private static final String REGISTRAR =
      "INSERT INTO sessoes_access (jti, usuario_id, expira_em)"
          + " VALUES (:jti, :usuarioId, :expiraEm)"
          + " ON CONFLICT (jti) DO UPDATE"
          + " SET usuario_id = EXCLUDED.usuario_id, expira_em = EXCLUDED.expira_em";

  private static final String BUSCAR_ATIVO =
      "SELECT 1 FROM sessoes_access WHERE jti = :jti AND expira_em > CURRENT_TIMESTAMP";

  private static final String REMOVER_POR_JTI = "DELETE FROM sessoes_access WHERE jti = :jti";

  private static final String REMOVER_POR_USUARIO =
      "DELETE FROM sessoes_access WHERE usuario_id = :usuarioId";

  @PersistenceContext EntityManager entityManager;

  @Override
  @Transactional
  public void registrarAccess(String jti, Long usuarioId, long ttlSegundos) {
    entityManager
        .createNativeQuery(REGISTRAR)
        .setParameter("jti", jti)
        .setParameter("usuarioId", usuarioId)
        .setParameter("expiraEm", Timestamp.valueOf(LocalDateTime.now().plusSeconds(ttlSegundos)))
        .executeUpdate();
  }

  @Override
  public boolean accessAtivo(String jti) {
    return !entityManager
        .createNativeQuery(BUSCAR_ATIVO)
        .setParameter("jti", jti)
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  @Override
  @Transactional
  public void revogarAccess(String jti) {
    entityManager.createNativeQuery(REMOVER_POR_JTI).setParameter("jti", jti).executeUpdate();
  }

  @Override
  @Transactional
  public void invalidarAccessPorUsuario(Long usuarioId) {
    entityManager
        .createNativeQuery(REMOVER_POR_USUARIO)
        .setParameter("usuarioId", usuarioId)
        .executeUpdate();
  }
}

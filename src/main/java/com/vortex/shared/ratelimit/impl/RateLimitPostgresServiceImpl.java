package com.vortex.shared.ratelimit.impl;

import com.vortex.shared.ratelimit.RateLimitService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.hibernate.Session;

/**
 * Contadores de rate limit na tabela UNLOGGED {@code rate_limits}. O upsert com RETURNING é um
 * único statement atômico, o que garante limite global exato mesmo com múltiplas instâncias do
 * backend incrementando a mesma chave concorrentemente.
 */
@ApplicationScoped
public class RateLimitPostgresServiceImpl implements RateLimitService {

  private static final String SQL_CONSUMIR =
      "INSERT INTO rate_limits (chave, contagem, expira_em) VALUES (?, 1, ?)"
          + " ON CONFLICT (chave) DO UPDATE SET"
          + " contagem = CASE WHEN rate_limits.expira_em <= CURRENT_TIMESTAMP"
          + " THEN 1 ELSE rate_limits.contagem + 1 END,"
          + " expira_em = CASE WHEN rate_limits.expira_em <= CURRENT_TIMESTAMP"
          + " THEN EXCLUDED.expira_em ELSE rate_limits.expira_em END"
          + " RETURNING contagem";

  @PersistenceContext EntityManager entityManager;

  @Override
  @Transactional
  public boolean tentarConsumir(String chave, int limite, long janelaSegundos) {
    Timestamp expiraEm = Timestamp.valueOf(LocalDateTime.now().plusSeconds(janelaSegundos));
    long contagem =
        entityManager
            .unwrap(Session.class)
            .doReturningWork(connection -> consumir(connection, chave, expiraEm));
    return contagem <= limite;
  }

  private long consumir(java.sql.Connection connection, String chave, Timestamp expiraEm)
      throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(SQL_CONSUMIR)) {
      statement.setString(1, chave);
      statement.setTimestamp(2, expiraEm);
      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getLong(1);
      }
    }
  }
}

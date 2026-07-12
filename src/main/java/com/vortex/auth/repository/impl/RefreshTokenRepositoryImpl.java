package com.vortex.auth.repository.impl;

import com.vortex.auth.entity.RefreshToken;
import com.vortex.auth.repository.RefreshTokenQuery;
import com.vortex.auth.repository.RefreshTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public RefreshToken save(RefreshToken refreshToken) {
    if (refreshToken.getId() == null) {
      entityManager.persist(refreshToken);
      return refreshToken;
    }
    return entityManager.merge(refreshToken);
  }

  @Override
  public Optional<RefreshToken> findValidoPorHash(String tokenHash) {
    return entityManager
        .createNativeQuery(RefreshTokenQuery.BUSCAR_VALIDO_POR_HASH.getSql(), RefreshToken.class)
        .setParameter("tokenHash", tokenHash)
        .getResultStream()
        .findFirst();
  }

  @Override
  public Optional<RefreshToken> findValidoPorHashForUpdate(String tokenHash) {
    return entityManager
        .createNativeQuery(
            RefreshTokenQuery.BUSCAR_VALIDO_POR_HASH_FOR_UPDATE.getSql(), RefreshToken.class)
        .setParameter("tokenHash", tokenHash)
        .getResultStream()
        .findFirst();
  }

  @Override
  public Optional<RefreshToken> findPorHash(String tokenHash) {
    return entityManager
        .createNativeQuery(RefreshTokenQuery.BUSCAR_POR_HASH.getSql(), RefreshToken.class)
        .setParameter("tokenHash", tokenHash)
        .getResultStream()
        .findFirst();
  }

  @Override
  public void revogarPorHash(String tokenHash) {
    entityManager
        .createNativeQuery(RefreshTokenQuery.REVOGAR_POR_HASH.getSql())
        .setParameter("tokenHash", tokenHash)
        .executeUpdate();
  }

  @Override
  public void revogarPorUsuarioId(Long usuarioId) {
    entityManager
        .createNativeQuery(RefreshTokenQuery.REVOGAR_POR_USUARIO_ID.getSql())
        .setParameter("usuarioId", usuarioId)
        .executeUpdate();
  }

  @Override
  public int removerExpiradosERevogados() {
    return entityManager
        .createNativeQuery(RefreshTokenQuery.REMOVER_EXPIRADOS_E_REVOGADOS.getSql())
        .executeUpdate();
  }
}

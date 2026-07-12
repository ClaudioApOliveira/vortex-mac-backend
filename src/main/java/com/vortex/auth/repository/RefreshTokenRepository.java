package com.vortex.auth.repository;

import com.vortex.auth.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository {

  RefreshToken save(RefreshToken refreshToken);

  Optional<RefreshToken> findValidoPorHash(String tokenHash);

  Optional<RefreshToken> findValidoPorHashForUpdate(String tokenHash);

  Optional<RefreshToken> findPorHash(String tokenHash);

  void revogarPorHash(String tokenHash);

  void revogarPorUsuarioId(Long usuarioId);

  int removerExpiradosERevogados();
}

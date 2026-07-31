package com.vortex.auth.security.impl;

import com.vortex.auth.entity.RefreshToken;
import com.vortex.auth.repository.RefreshTokenRepository;
import com.vortex.auth.security.AuthMessages;
import com.vortex.auth.security.RefreshTokenService;
import com.vortex.auth.security.TokenHashUtil;
import com.vortex.shared.exception.UnauthorizedException;
import com.vortex.usuario.entity.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @ConfigProperty(name = "vortex.jwt.refresh-token.lifespan", defaultValue = "604800")
  long refreshTokenExpiraEmSegundos;

  @Inject
  public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  @Transactional
  public String criar(Usuario usuario) {
    String tokenPlano = gerarTokenPlano();
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setTokenHash(TokenHashUtil.hash(tokenPlano));
    refreshToken.setUsuario(usuario);
    refreshToken.setExpiraEm(LocalDateTime.now().plusSeconds(refreshTokenExpiraEmSegundos));

    refreshTokenRepository.save(refreshToken);
    return tokenPlano;
  }

  @Override
  @Transactional
  public RefreshToken validarERevogar(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO);
    }

    String hash = TokenHashUtil.hash(refreshToken);
    RefreshToken refreshTokenExistente =
        refreshTokenRepository
            .findPorHash(hash)
            .orElseThrow(() -> new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO));

    if (refreshTokenExistente.isRevogado()) {
      revogarTodosPorUsuario(refreshTokenExistente.getUsuario().getId());
      throw new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO);
    }

    RefreshToken refreshTokenEntidade =
        refreshTokenRepository
            .findValidoPorHashForUpdate(hash)
            .orElseThrow(() -> new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO));

    refreshTokenRepository.revogarPorHash(hash);
    return refreshTokenEntidade;
  }

  @Override
  @Transactional
  public void revogar(String refreshToken) {
    refreshTokenRepository.revogarPorHash(TokenHashUtil.hash(refreshToken));
  }

  @Override
  @Transactional
  public void revogarTodosPorUsuario(Long usuarioId) {
    refreshTokenRepository.revogarPorUsuarioId(usuarioId);
  }

  @Override
  @Transactional
  public void removerTodosPorUsuario(Long usuarioId) {
    refreshTokenRepository.removerPorUsuarioId(usuarioId);
  }

  @Override
  public long getRefreshTokenExpiraEmSegundos() {
    return refreshTokenExpiraEmSegundos;
  }

  private String gerarTokenPlano() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }
}

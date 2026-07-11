package com.vortex.auth.security.impl;

import com.vortex.auth.entity.RefreshToken;
import com.vortex.auth.repository.RefreshTokenRepository;
import com.vortex.auth.security.AuthMessages;
import com.vortex.auth.security.RefreshTokenService;
import com.vortex.auth.security.SessaoService;
import com.vortex.shared.exception.UnauthorizedException;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final RefreshTokenRepository refreshTokenRepository;
  private final UsuarioRepository usuarioRepository;
  private final SessaoService sessaoService;

  @ConfigProperty(name = "vortex.jwt.refresh-token.lifespan", defaultValue = "604800")
  long refreshTokenExpiraEmSegundos;

  @Inject
  public RefreshTokenServiceImpl(
      RefreshTokenRepository refreshTokenRepository,
      UsuarioRepository usuarioRepository,
      SessaoService sessaoService) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.usuarioRepository = usuarioRepository;
    this.sessaoService = sessaoService;
  }

  @Override
  @Transactional
  public String criar(Long usuarioId) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

    String tokenPlano = gerarTokenPlano();
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setTokenHash(hashToken(tokenPlano));
    refreshToken.setUsuario(usuario);
    refreshToken.setExpiraEm(LocalDateTime.now().plusSeconds(refreshTokenExpiraEmSegundos));

    refreshTokenRepository.save(refreshToken);
    sessaoService.liberarRefreshPorUsuario(usuarioId);
    sessaoService.registrarRefresh(tokenPlano, usuarioId, refreshTokenExpiraEmSegundos);
    return tokenPlano;
  }

  @Override
  @Transactional
  public void revogar(String refreshToken) {
    refreshTokenRepository.revogarPorHash(hashToken(refreshToken));
    sessaoService.revogarRefresh(refreshToken);
  }

  @Override
  @Transactional
  public void revogarTodosPorUsuario(Long usuarioId) {
    refreshTokenRepository.revogarPorUsuarioId(usuarioId);
    sessaoService.invalidarRefreshPorUsuario(usuarioId, refreshTokenExpiraEmSegundos);
  }

  @Override
  @Transactional
  public RefreshToken validar(String refreshToken) {
    RefreshToken refreshTokenEntidade =
        refreshTokenRepository
            .findValidoPorHash(hashToken(refreshToken))
            .orElseThrow(() -> new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO));

    Long usuarioId = refreshTokenEntidade.getUsuario().getId();
    if (sessaoService.refreshInvalidadoPorUsuario(usuarioId)) {
      throw new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO);
    }

    if (!sessaoService.refreshAtivo(refreshToken)) {
      throw new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO);
    }

    return refreshTokenEntidade;
  }

  @Override
  public long getRefreshTokenExpiraEmSegundos() {
    return refreshTokenExpiraEmSegundos;
  }

  private String gerarTokenPlano() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("Algoritmo SHA-256 não disponível", exception);
    }
  }
}

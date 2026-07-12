package com.vortex.auth.service;

import com.vortex.auth.dto.TokensGerados;
import com.vortex.auth.security.AccessTokenGerado;
import com.vortex.auth.security.JwtService;
import com.vortex.auth.security.RefreshTokenService;
import com.vortex.auth.security.SessaoService;
import com.vortex.shared.exception.UnauthorizedException;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthTokenWriter {

  private final UsuarioRepository usuarioRepository;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final SessaoService sessaoService;

  @Inject
  public AuthTokenWriter(
      UsuarioRepository usuarioRepository,
      JwtService jwtService,
      RefreshTokenService refreshTokenService,
      SessaoService sessaoService) {
    this.usuarioRepository = usuarioRepository;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.sessaoService = sessaoService;
  }

  @Transactional
  public TokensGerados gerarTokens(Long usuarioId) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    return gerarTokens(usuario);
  }

  @Transactional
  public TokensGerados definirSenhaEGerarTokens(Long usuarioId, String senhaHash) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    usuario.setSenha(senhaHash);
    usuario.setDeveDefinirSenha(false);
    return gerarTokens(usuario);
  }

  @Transactional
  public TokensGerados alterarSenhaEGerarTokens(Long usuarioId, String novaSenhaHash, String jti) {
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    usuario.setSenha(novaSenhaHash);

    if (jti != null) {
      sessaoService.revogarAccess(jti);
    }

    refreshTokenService.revogarTodosPorUsuario(usuarioId);
    return gerarTokens(usuario);
  }

  private TokensGerados gerarTokens(Usuario usuario) {
    Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
    AccessTokenGerado accessTokenGerado =
        jwtService.gerarToken(
            usuario.getId(),
            usuario.getEmail(),
            usuario.getNome(),
            usuario.getPerfil().name(),
            clienteId);

    sessaoService.registrarAccess(
        accessTokenGerado.jti(), usuario.getId(), jwtService.getAccessTokenExpiraEmSegundos());

    String refreshToken = refreshTokenService.criar(usuario);

    return new TokensGerados(
        accessTokenGerado.token(),
        refreshToken,
        "Bearer",
        jwtService.getAccessTokenExpiraEmSegundos(),
        refreshTokenService.getRefreshTokenExpiraEmSegundos(),
        usuario.isDeveDefinirSenha());
  }
}

package com.vortex.auth.service.impl;

import com.vortex.auth.service.UsuarioAutenticadoProvider;
import com.vortex.shared.exception.UnauthorizedException;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class UsuarioAutenticadoProviderImpl implements UsuarioAutenticadoProvider {

  private final SecurityIdentity securityIdentity;
  private final Instance<JsonWebToken> jwt;
  private final UsuarioRepository usuarioRepository;

  @Inject
  public UsuarioAutenticadoProviderImpl(
      SecurityIdentity securityIdentity,
      Instance<JsonWebToken> jwt,
      UsuarioRepository usuarioRepository) {
    this.securityIdentity = securityIdentity;
    this.jwt = jwt;
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  public Optional<Usuario> obterUsuarioAutenticado() {
    if (securityIdentity.isAnonymous() || !jwt.isResolvable()) {
      return Optional.empty();
    }

    try {
      long usuarioId = Long.parseLong(jwt.get().getSubject());
      return usuarioRepository.findById(usuarioId);
    } catch (NumberFormatException exception) {
      return Optional.empty();
    }
  }

  @Override
  public Usuario obterUsuarioAutenticadoObrigatorio() {
    return obterUsuarioAutenticado()
        .orElseThrow(() -> new UnauthorizedException("Usuário não autenticado"));
  }
}

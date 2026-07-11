package com.vortex.shared.auditoria;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class AuditoriaListener {

  private final SecurityIdentity securityIdentity;
  private final Instance<JsonWebToken> jwt;

  @Inject
  public AuditoriaListener(SecurityIdentity securityIdentity, Instance<JsonWebToken> jwt) {
    this.securityIdentity = securityIdentity;
    this.jwt = jwt;
  }

  @PrePersist
  public void aoCriar(Auditoria entidade) {
    LocalDateTime agora = LocalDateTime.now();
    entidade.setCriadoEm(agora);
    entidade.setAtualizadoEm(agora);
    obterUsuarioLogadoId()
        .ifPresent(
            usuarioId -> {
              entidade.setUsuarioInclusao(usuarioId);
              entidade.setUsuarioAlteracao(usuarioId);
            });
  }

  @PreUpdate
  public void aoAtualizar(Auditoria entidade) {
    entidade.setAtualizadoEm(LocalDateTime.now());
    obterUsuarioLogadoId().ifPresent(entidade::setUsuarioAlteracao);
  }

  private Optional<Long> obterUsuarioLogadoId() {
    if (securityIdentity.isAnonymous() || !jwt.isResolvable()) {
      return Optional.empty();
    }

    try {
      return Optional.of(Long.parseLong(jwt.get().getSubject()));
    } catch (NumberFormatException exception) {
      return Optional.empty();
    }
  }
}

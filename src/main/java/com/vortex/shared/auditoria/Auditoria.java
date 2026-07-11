package com.vortex.shared.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditoriaListener.class)
public abstract class Auditoria {

  @Column(name = "criado_em", nullable = false, updatable = false)
  private LocalDateTime criadoEm;

  @Column(name = "atualizado_em", nullable = false)
  private LocalDateTime atualizadoEm;

  @Column(name = "usuario_inclusao_id", updatable = false)
  private Long usuarioInclusao;

  @Column(name = "usuario_alteracao_id")
  private Long usuarioAlteracao;

  public LocalDateTime getCriadoEm() {
    return criadoEm;
  }

  public void setCriadoEm(LocalDateTime criadoEm) {
    this.criadoEm = criadoEm;
  }

  public LocalDateTime getAtualizadoEm() {
    return atualizadoEm;
  }

  public void setAtualizadoEm(LocalDateTime atualizadoEm) {
    this.atualizadoEm = atualizadoEm;
  }

  public Long getUsuarioInclusao() {
    return usuarioInclusao;
  }

  public void setUsuarioInclusao(Long usuarioInclusao) {
    this.usuarioInclusao = usuarioInclusao;
  }

  public Long getUsuarioAlteracao() {
    return usuarioAlteracao;
  }

  public void setUsuarioAlteracao(Long usuarioAlteracao) {
    this.usuarioAlteracao = usuarioAlteracao;
  }
}

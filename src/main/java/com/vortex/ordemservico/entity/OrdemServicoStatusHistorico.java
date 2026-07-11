package com.vortex.ordemservico.entity;

import com.vortex.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordens_servico_status_historico")
public class OrdemServicoStatusHistorico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ordem_servico_id", nullable = false)
  private OrdemServico ordemServico;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_anterior", length = 30)
  private OrdemServicoStatus statusAnterior;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_novo", nullable = false, length = 30)
  private OrdemServicoStatus statusNovo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private OrdemServicoStatusHistoricoOrigem origem = OrdemServicoStatusHistoricoOrigem.SISTEMA;

  @Column(length = 500)
  private String observacao;

  @Column(name = "criado_em", nullable = false)
  private LocalDateTime criadoEm;

  @PrePersist
  public void prePersist() {
    if (criadoEm == null) {
      criadoEm = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OrdemServico getOrdemServico() {
    return ordemServico;
  }

  public void setOrdemServico(OrdemServico ordemServico) {
    this.ordemServico = ordemServico;
  }

  public OrdemServicoStatus getStatusAnterior() {
    return statusAnterior;
  }

  public void setStatusAnterior(OrdemServicoStatus statusAnterior) {
    this.statusAnterior = statusAnterior;
  }

  public OrdemServicoStatus getStatusNovo() {
    return statusNovo;
  }

  public void setStatusNovo(OrdemServicoStatus statusNovo) {
    this.statusNovo = statusNovo;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public OrdemServicoStatusHistoricoOrigem getOrigem() {
    return origem;
  }

  public void setOrigem(OrdemServicoStatusHistoricoOrigem origem) {
    this.origem = origem;
  }

  public String getObservacao() {
    return observacao;
  }

  public void setObservacao(String observacao) {
    this.observacao = observacao;
  }

  public LocalDateTime getCriadoEm() {
    return criadoEm;
  }

  public void setCriadoEm(LocalDateTime criadoEm) {
    this.criadoEm = criadoEm;
  }
}

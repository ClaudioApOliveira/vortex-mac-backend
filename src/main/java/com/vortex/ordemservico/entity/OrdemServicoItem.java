package com.vortex.ordemservico.entity;

import com.vortex.shared.auditoria.Auditoria;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "ordens_servico_itens")
public class OrdemServicoItem extends Auditoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ordem_servico_id", nullable = false)
  private OrdemServico ordemServico;

  @Column(nullable = false, length = 255)
  private String descricao;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal quantidade = BigDecimal.ONE;

  @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
  private BigDecimal valorUnitario = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrdemServicoItemTipo tipo;

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

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public BigDecimal getQuantidade() {
    return quantidade;
  }

  public void setQuantidade(BigDecimal quantidade) {
    this.quantidade = quantidade;
  }

  public BigDecimal getValorUnitario() {
    return valorUnitario;
  }

  public void setValorUnitario(BigDecimal valorUnitario) {
    this.valorUnitario = valorUnitario;
  }

  public OrdemServicoItemTipo getTipo() {
    return tipo;
  }

  public void setTipo(OrdemServicoItemTipo tipo) {
    this.tipo = tipo;
  }
}

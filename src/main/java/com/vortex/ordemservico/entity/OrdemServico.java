package com.vortex.ordemservico.entity;

import com.vortex.cliente.entity.Cliente;
import com.vortex.shared.auditoria.Auditoria;
import com.vortex.usuario.entity.Usuario;
import com.vortex.veiculo.entity.Veiculo;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordens_servico")
public class OrdemServico extends Auditoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Cliente cliente;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "veiculo_id", nullable = false)
  private Veiculo veiculo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tecnico_id", nullable = false)
  private Usuario tecnico;

  @Column(nullable = false)
  private LocalDate data;

  @Column(nullable = false)
  private LocalTime hora;

  @Column(name = "km_entrada")
  private Integer kmEntrada;

  @Column(name = "km_saida")
  private Integer kmSaida;

  @Column(name = "custo_servicos_terceirizados", nullable = false, precision = 12, scale = 2)
  private BigDecimal custoServicosTerceirizados = BigDecimal.ZERO;

  @Column(name = "descricao_servicos_terceirizados", length = 500)
  private String descricaoServicosTerceirizados;

  @Column(name = "custo_pecas", nullable = false, precision = 12, scale = 2)
  private BigDecimal custoPecas = BigDecimal.ZERO;

  @Column(name = "custo_mao_de_obra", nullable = false, precision = 12, scale = 2)
  private BigDecimal custoMaoDeObra = BigDecimal.ZERO;

  @Column(name = "descricao_mao_de_obra", length = 500)
  private String descricaoMaoDeObra;

  @Column(name = "preco_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal precoTotal = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private OrdemServicoStatus status = OrdemServicoStatus.ORCAMENTO;

  @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrdemServicoItem> itens = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public Veiculo getVeiculo() {
    return veiculo;
  }

  public void setVeiculo(Veiculo veiculo) {
    this.veiculo = veiculo;
  }

  public Usuario getTecnico() {
    return tecnico;
  }

  public void setTecnico(Usuario tecnico) {
    this.tecnico = tecnico;
  }

  public LocalDate getData() {
    return data;
  }

  public void setData(LocalDate data) {
    this.data = data;
  }

  public LocalTime getHora() {
    return hora;
  }

  public void setHora(LocalTime hora) {
    this.hora = hora;
  }

  public Integer getKmEntrada() {
    return kmEntrada;
  }

  public void setKmEntrada(Integer kmEntrada) {
    this.kmEntrada = kmEntrada;
  }

  public Integer getKmSaida() {
    return kmSaida;
  }

  public void setKmSaida(Integer kmSaida) {
    this.kmSaida = kmSaida;
  }

  public BigDecimal getCustoServicosTerceirizados() {
    return custoServicosTerceirizados;
  }

  public void setCustoServicosTerceirizados(BigDecimal custoServicosTerceirizados) {
    this.custoServicosTerceirizados = custoServicosTerceirizados;
  }

  public String getDescricaoServicosTerceirizados() {
    return descricaoServicosTerceirizados;
  }

  public void setDescricaoServicosTerceirizados(String descricaoServicosTerceirizados) {
    this.descricaoServicosTerceirizados = descricaoServicosTerceirizados;
  }

  public BigDecimal getCustoPecas() {
    return custoPecas;
  }

  public void setCustoPecas(BigDecimal custoPecas) {
    this.custoPecas = custoPecas;
  }

  public BigDecimal getCustoMaoDeObra() {
    return custoMaoDeObra;
  }

  public void setCustoMaoDeObra(BigDecimal custoMaoDeObra) {
    this.custoMaoDeObra = custoMaoDeObra;
  }

  public String getDescricaoMaoDeObra() {
    return descricaoMaoDeObra;
  }

  public void setDescricaoMaoDeObra(String descricaoMaoDeObra) {
    this.descricaoMaoDeObra = descricaoMaoDeObra;
  }

  public BigDecimal getPrecoTotal() {
    return precoTotal;
  }

  public void setPrecoTotal(BigDecimal precoTotal) {
    this.precoTotal = precoTotal;
  }

  public OrdemServicoStatus getStatus() {
    return status;
  }

  public void setStatus(OrdemServicoStatus status) {
    this.status = status;
  }

  public List<OrdemServicoItem> getItens() {
    return itens;
  }

  public void setItens(List<OrdemServicoItem> itens) {
    this.itens = itens;
  }
}

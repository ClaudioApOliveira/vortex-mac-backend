package com.vortex.veiculo.entity;

import com.vortex.cliente.entity.Cliente;
import com.vortex.shared.auditoria.Auditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "veiculos")
public class Veiculo extends Auditoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 10)
  private String placa;

  @Column(nullable = false, length = 80)
  private String marca;

  @Column(nullable = false, length = 120)
  private String modelo;

  @Column(name = "ano_fabricacao", nullable = false)
  private Integer anoFabricacao;

  @Column(length = 30)
  private String motor;

  @Column(length = 30)
  private String combustivel;

  @Column(name = "km_atual")
  private Integer kmAtual;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Cliente cliente;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPlaca() {
    return placa;
  }

  public void setPlaca(String placa) {
    this.placa = placa;
  }

  public String getMarca() {
    return marca;
  }

  public void setMarca(String marca) {
    this.marca = marca;
  }

  public String getModelo() {
    return modelo;
  }

  public void setModelo(String modelo) {
    this.modelo = modelo;
  }

  public Integer getAnoFabricacao() {
    return anoFabricacao;
  }

  public void setAnoFabricacao(Integer anoFabricacao) {
    this.anoFabricacao = anoFabricacao;
  }

  public String getMotor() {
    return motor;
  }

  public void setMotor(String motor) {
    this.motor = motor;
  }

  public String getCombustivel() {
    return combustivel;
  }

  public void setCombustivel(String combustivel) {
    this.combustivel = combustivel;
  }

  public Integer getKmAtual() {
    return kmAtual;
  }

  public void setKmAtual(Integer kmAtual) {
    this.kmAtual = kmAtual;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }
}

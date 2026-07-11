package com.vortex.usuario.entity;

import com.vortex.cliente.entity.Cliente;
import com.vortex.shared.auditoria.Auditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario extends Auditoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(length = 255)
  private String senha;

  @Column(name = "deve_definir_senha", nullable = false)
  private boolean deveDefinirSenha = false;

  @Column(nullable = false, length = 150)
  private String nome;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Perfil perfil;

  @OneToOne
  @JoinColumn(name = "cliente_id", unique = true)
  private Cliente cliente;

  @Column(nullable = false)
  private boolean ativo = true;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getSenha() {
    return senha;
  }

  public void setSenha(String senha) {
    this.senha = senha;
  }

  public boolean isDeveDefinirSenha() {
    return deveDefinirSenha;
  }

  public void setDeveDefinirSenha(boolean deveDefinirSenha) {
    this.deveDefinirSenha = deveDefinirSenha;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public Perfil getPerfil() {
    return perfil;
  }

  public void setPerfil(Perfil perfil) {
    this.perfil = perfil;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void setAtivo(boolean ativo) {
    this.ativo = ativo;
  }
}

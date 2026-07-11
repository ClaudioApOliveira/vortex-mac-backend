package com.vortex.cliente.entity;

import com.vortex.endereco.entity.Endereco;
import com.vortex.shared.auditoria.Auditoria;
import com.vortex.usuario.entity.Usuario;
import jakarta.persistence.CascadeType;
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
@Table(name = "clientes")
public class Cliente extends Auditoria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String nome;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_pessoa", nullable = false, length = 20)
  private TipoPessoa tipoPessoa = TipoPessoa.PESSOA_FISICA;

  @Column(unique = true, length = 14)
  private String cpf;

  @Column(unique = true, length = 18)
  private String cnpj;

  @Column(name = "razao_social", length = 150)
  private String razaoSocial;

  @Column(name = "nome_fantasia", length = 150)
  private String nomeFantasia;

  @Column(length = 20)
  private String telefone;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "endereco_id", unique = true)
  private Endereco endereco;

  @OneToOne(mappedBy = "cliente")
  private Usuario usuario;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public TipoPessoa getTipoPessoa() {
    return tipoPessoa;
  }

  public void setTipoPessoa(TipoPessoa tipoPessoa) {
    this.tipoPessoa = tipoPessoa;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getCnpj() {
    return cnpj;
  }

  public void setCnpj(String cnpj) {
    this.cnpj = cnpj;
  }

  public String getRazaoSocial() {
    return razaoSocial;
  }

  public void setRazaoSocial(String razaoSocial) {
    this.razaoSocial = razaoSocial;
  }

  public String getNomeFantasia() {
    return nomeFantasia;
  }

  public void setNomeFantasia(String nomeFantasia) {
    this.nomeFantasia = nomeFantasia;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String telefone) {
    this.telefone = telefone;
  }

  public Endereco getEndereco() {
    return endereco;
  }

  public void setEndereco(Endereco endereco) {
    this.endereco = endereco;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
}

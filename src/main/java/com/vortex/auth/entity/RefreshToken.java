package com.vortex.auth.entity;

import com.vortex.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @ManyToOne(optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(name = "expira_em", nullable = false)
  private LocalDateTime expiraEm;

  @Column(nullable = false)
  private boolean revogado = false;

  @Column(name = "criado_em", nullable = false, updatable = false)
  private LocalDateTime criadoEm;

  @PrePersist
  void aoCriar() {
    criadoEm = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String tokenHash) {
    this.tokenHash = tokenHash;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public LocalDateTime getExpiraEm() {
    return expiraEm;
  }

  public void setExpiraEm(LocalDateTime expiraEm) {
    this.expiraEm = expiraEm;
  }

  public boolean isRevogado() {
    return revogado;
  }

  public void setRevogado(boolean revogado) {
    this.revogado = revogado;
  }

  public LocalDateTime getCriadoEm() {
    return criadoEm;
  }
}

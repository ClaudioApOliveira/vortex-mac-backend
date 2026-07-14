package com.vortex.usuario.repository;

public enum UsuarioQuery {
  BUSCAR_POR_ID("SELECT * FROM usuarios WHERE id = :id"),
  LISTAR_TODOS("SELECT * FROM usuarios"),
  LISTAR_POR_PERFIL("SELECT * FROM usuarios WHERE perfil = :perfil AND ativo = TRUE ORDER BY nome"),
  BUSCAR_POR_EMAIL("SELECT * FROM usuarios WHERE email = :email"),
  CONTAR_POR_EMAIL("SELECT COUNT(*) FROM usuarios WHERE email = :email"),
  CONTAR_POR_EMAIL_E_ID_DIFERENTE(
      "SELECT COUNT(*) FROM usuarios WHERE email = :email AND id <> :id"),
  CONTAR_POR_CLIENTE_ID("SELECT COUNT(*) FROM usuarios WHERE cliente_id = :clienteId"),
  CONTAR_POR_CLIENTE_ID_E_ID_DIFERENTE(
      "SELECT COUNT(*) FROM usuarios WHERE cliente_id = :clienteId AND id <> :id"),
  CONTAR_ADMINS_ATIVOS("SELECT COUNT(*) FROM usuarios WHERE perfil = 'ADMIN' AND ativo = TRUE");

  private final String sql;

  UsuarioQuery(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

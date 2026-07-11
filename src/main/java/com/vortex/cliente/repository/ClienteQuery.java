package com.vortex.cliente.repository;

public enum ClienteQuery {
  BUSCAR_POR_ID("SELECT * FROM clientes WHERE id = :id"),
  LISTAR_TODOS("SELECT * FROM clientes"),
  BUSCAR_POR_CPF("SELECT * FROM clientes WHERE cpf = :cpf"),
  CONTAR_POR_CPF("SELECT COUNT(*) FROM clientes WHERE cpf = :cpf"),
  CONTAR_POR_CPF_E_ID_DIFERENTE("SELECT COUNT(*) FROM clientes WHERE cpf = :cpf AND id <> :id"),
  CONTAR_POR_CNPJ("SELECT COUNT(*) FROM clientes WHERE cnpj = :cnpj"),
  CONTAR_POR_CNPJ_E_ID_DIFERENTE("SELECT COUNT(*) FROM clientes WHERE cnpj = :cnpj AND id <> :id");

  private final String sql;

  ClienteQuery(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

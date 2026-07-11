package com.vortex.endereco.repository;

public enum EnderecoQuery {
  BUSCAR_POR_ID("SELECT * FROM enderecos WHERE id = :id");

  private final String sql;

  EnderecoQuery(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

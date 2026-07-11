package com.vortex.veiculo.repository;

public enum VeiculoQuery {
  CONTAR_POR_PLACA("SELECT COUNT(*) FROM veiculos WHERE placa = :placa"),
  CONTAR_POR_PLACA_E_ID_DIFERENTE(
      "SELECT COUNT(*) FROM veiculos WHERE placa = :placa AND id <> :id");

  private final String sql;

  VeiculoQuery(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

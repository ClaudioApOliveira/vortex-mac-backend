package com.vortex.auth.repository;

public enum RefreshTokenQuery {
  BUSCAR_VALIDO_POR_HASH(
      "SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash AND revogado = FALSE AND"
          + " expira_em > CURRENT_TIMESTAMP"),
  BUSCAR_VALIDO_POR_HASH_FOR_UPDATE(
      "SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash AND revogado = FALSE AND"
          + " expira_em > CURRENT_TIMESTAMP FOR UPDATE"),
  BUSCAR_POR_HASH("SELECT * FROM refresh_tokens WHERE token_hash = :tokenHash"),
  REVOGAR_POR_HASH("UPDATE refresh_tokens SET revogado = TRUE WHERE token_hash = :tokenHash"),
  REVOGAR_POR_USUARIO_ID(
      "UPDATE refresh_tokens SET revogado = TRUE WHERE usuario_id = :usuarioId AND revogado ="
          + " FALSE"),
  REMOVER_POR_USUARIO_ID("DELETE FROM refresh_tokens WHERE usuario_id = :usuarioId"),
  REMOVER_EXPIRADOS_E_REVOGADOS(
      "DELETE FROM refresh_tokens WHERE expira_em < CURRENT_TIMESTAMP OR revogado = TRUE");

  private final String sql;

  RefreshTokenQuery(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

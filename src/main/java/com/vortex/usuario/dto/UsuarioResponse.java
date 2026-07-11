package com.vortex.usuario.dto;

import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import java.time.LocalDateTime;

public record UsuarioResponse(
    Long id,
    String email,
    String nome,
    Perfil perfil,
    Long clienteId,
    boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static UsuarioResponse from(Usuario usuario) {
    Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
    return new UsuarioResponse(
        usuario.getId(),
        usuario.getEmail(),
        usuario.getNome(),
        usuario.getPerfil(),
        clienteId,
        usuario.isAtivo(),
        usuario.getCriadoEm(),
        usuario.getAtualizadoEm());
  }
}

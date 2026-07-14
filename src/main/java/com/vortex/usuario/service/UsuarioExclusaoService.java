package com.vortex.usuario.service;

/** Exclusão segura de usuário com limpeza de sessões, tokens e vínculos. */
public interface UsuarioExclusaoService {

  /**
   * Exclusão solicitada via API de usuários: valida autoexclusão, último ADMIN e ordens de serviço.
   */
  void excluir(Long usuarioId);

  /**
   * Exclusão do usuário vinculado ao apagar um cliente: limpa dependências sem regras de
   * autoexclusão/último ADMIN.
   */
  void excluirAoRemoverCliente(Long usuarioId);
}

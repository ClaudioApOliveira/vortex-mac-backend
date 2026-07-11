package com.vortex.cliente.dto;

import com.vortex.cliente.entity.Cliente;
import com.vortex.cliente.entity.TipoPessoa;
import com.vortex.endereco.dto.EnderecoResponse;
import com.vortex.usuario.entity.Usuario;
import java.time.LocalDateTime;

public record ClienteResponse(
    Long id,
    TipoPessoa tipoPessoa,
    String email,
    String nome,
    String razaoSocial,
    String nomeFantasia,
    EnderecoResponse endereco,
    String cpf,
    String cnpj,
    String telefone,
    Long usuarioId,
    boolean deveDefinirSenha,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static ClienteResponse from(Cliente cliente) {
    Usuario usuario = cliente.getUsuario();
    Long usuarioId = usuario != null ? usuario.getId() : null;
    String email = usuario != null ? usuario.getEmail() : null;
    boolean deveDefinirSenha = usuario != null && usuario.isDeveDefinirSenha();

    return new ClienteResponse(
        cliente.getId(),
        cliente.getTipoPessoa(),
        email,
        cliente.getNome(),
        cliente.getRazaoSocial(),
        cliente.getNomeFantasia(),
        EnderecoResponse.from(cliente.getEndereco()),
        cliente.getCpf(),
        cliente.getCnpj(),
        cliente.getTelefone(),
        usuarioId,
        deveDefinirSenha,
        cliente.getCriadoEm(),
        cliente.getAtualizadoEm());
  }
}

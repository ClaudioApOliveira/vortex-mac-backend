package com.vortex.cliente.dto;

import com.vortex.cliente.entity.TipoPessoa;
import com.vortex.endereco.dto.EnderecoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
    @NotNull(message = "Tipo de pessoa é obrigatório") TipoPessoa tipoPessoa,
    @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email,
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres") String nome,
    @Size(max = 150, message = "Razão social deve ter no máximo 150 caracteres") String razaoSocial,
    @Size(max = 150, message = "Nome fantasia deve ter no máximo 150 caracteres")
        String nomeFantasia,
    @Valid EnderecoRequest endereco,
    @Size(max = 14, message = "CPF deve ter no máximo 14 caracteres") String cpf,
    @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres") String cnpj,
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres") String telefone) {}

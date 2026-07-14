package com.vortex.usuario.dto;

import com.vortex.usuario.entity.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email,
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres") String senha,
    @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,
    @NotNull(message = "Perfil é obrigatório") Perfil perfil,
    Long clienteId,
    Boolean ativo) {}

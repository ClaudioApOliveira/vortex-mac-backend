package com.vortex.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrimeiroAcessoRequest(
    @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email,
    @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String senha,
    @NotBlank(message = "Confirmação de senha é obrigatória") String confirmarSenha,
    @AssertTrue(message = "É necessário aceitar a Política de Privacidade") boolean lgpdAceite,
    @NotBlank(message = "Versão da política é obrigatória")
        @Size(max = 20, message = "Versão deve ter no máximo 20 caracteres")
        String lgpdAceiteVersao) {}

package com.vortex.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LgpdAceiteRequest(
    @AssertTrue(message = "É necessário aceitar a Política de Privacidade") boolean lgpdAceite,
    @NotBlank(message = "Versão da política é obrigatória")
        @Size(max = 20, message = "Versão deve ter no máximo 20 caracteres")
        String lgpdAceiteVersao) {}

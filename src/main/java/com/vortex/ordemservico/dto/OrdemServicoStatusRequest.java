package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServicoStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdemServicoStatusRequest(
    @NotNull(message = "Status é obrigatório") OrdemServicoStatus status,
    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres") String observacao) {}

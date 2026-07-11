package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServicoItemTipo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OrdemServicoItemRequest(
    @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricao,
    @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
        BigDecimal quantidade,
    @NotNull(message = "Valor unitário é obrigatório")
        @DecimalMin(value = "0", message = "Valor unitário não pode ser negativo")
        BigDecimal valorUnitario,
    @NotNull(message = "Tipo do item é obrigatório") OrdemServicoItemTipo tipo) {}

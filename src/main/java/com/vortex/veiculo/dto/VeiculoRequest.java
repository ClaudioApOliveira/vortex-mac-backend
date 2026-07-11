package com.vortex.veiculo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VeiculoRequest(
    @NotNull(message = "Cliente é obrigatório") Long clienteId,
    @NotBlank(message = "Placa é obrigatória")
        @Size(max = 10, message = "Placa deve ter no máximo 10 caracteres")
        String placa,
    @NotBlank(message = "Marca é obrigatória")
        @Size(max = 80, message = "Marca deve ter no máximo 80 caracteres")
        String marca,
    @NotBlank(message = "Modelo é obrigatório")
        @Size(max = 120, message = "Modelo deve ter no máximo 120 caracteres")
        String modelo,
    @NotNull(message = "Ano de fabricação é obrigatório")
        @Min(value = 1900, message = "Ano de fabricação inválido")
        @Max(value = 2100, message = "Ano de fabricação inválido")
        Integer anoFabricacao,
    @Size(max = 30, message = "Motor deve ter no máximo 30 caracteres") String motor,
    @Size(max = 30, message = "Combustível deve ter no máximo 30 caracteres") String combustivel,
    @Min(value = 0, message = "Quilometragem não pode ser negativa") Integer kmAtual) {}

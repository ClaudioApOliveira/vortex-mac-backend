package com.vortex.endereco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
    @NotBlank(message = "CEP é obrigatório")
        @Size(max = 9, message = "CEP deve ter no máximo 9 caracteres")
        String cep,
    @Size(max = 255, message = "Logradouro deve ter no máximo 255 caracteres") String logradouro,
    @Size(max = 255, message = "Complemento deve ter no máximo 255 caracteres") String complemento,
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres") String numero,
    @Size(max = 150, message = "Bairro deve ter no máximo 150 caracteres") String bairro,
    @Size(max = 150, message = "Cidade deve ter no máximo 150 caracteres") String cidade,
    @Size(max = 2, message = "UF deve ter no máximo 2 caracteres") String uf,
    @Size(max = 100, message = "Estado deve ter no máximo 100 caracteres") String estado,
    @Size(max = 10, message = "IBGE deve ter no máximo 10 caracteres") String ibge) {}

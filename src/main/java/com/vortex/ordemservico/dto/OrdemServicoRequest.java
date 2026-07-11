package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServicoStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OrdemServicoRequest(
    @NotNull(message = "Cliente é obrigatório") Long clienteId,
    @NotNull(message = "Veículo é obrigatório") Long veiculoId,
    @NotNull(message = "Técnico é obrigatório") Long tecnicoId,
    @NotNull(message = "Data é obrigatória") LocalDate data,
    @NotNull(message = "Hora é obrigatória") LocalTime hora,
    @Min(value = 0, message = "KM de entrada não pode ser negativa") Integer kmEntrada,
    @Min(value = 0, message = "KM de saída não pode ser negativa") Integer kmSaida,
    @NotNull(message = "Custo de serviços terceirizados é obrigatório")
        @DecimalMin(value = "0", message = "Custo de serviços terceirizados não pode ser negativo")
        BigDecimal custoServicosTerceirizados,
    @Size(
            max = 500,
            message = "Descrição dos serviços terceirizados deve ter no máximo 500 caracteres")
        String descricaoServicosTerceirizados,
    @NotNull(message = "Custo de mão de obra é obrigatório")
        @DecimalMin(value = "0", message = "Custo de mão de obra não pode ser negativo")
        BigDecimal custoMaoDeObra,
    @Size(max = 500, message = "Descrição da mão de obra deve ter no máximo 500 caracteres")
        String descricaoMaoDeObra,
    OrdemServicoStatus status,
    @NotEmpty(message = "Informe ao menos um item de peça ou serviço") @Valid
        List<OrdemServicoItemRequest> itens) {}

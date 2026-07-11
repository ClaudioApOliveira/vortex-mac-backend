package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServico;
import com.vortex.ordemservico.entity.OrdemServicoStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OrdemServicoResponse(
    Long id,
    Long clienteId,
    String clienteNome,
    Long veiculoId,
    String veiculoPlaca,
    String veiculoMarca,
    String veiculoModelo,
    Long tecnicoId,
    String tecnicoNome,
    LocalDate data,
    LocalTime hora,
    Integer kmEntrada,
    Integer kmSaida,
    BigDecimal custoServicosTerceirizados,
    String descricaoServicosTerceirizados,
    BigDecimal custoPecas,
    BigDecimal custoMaoDeObra,
    String descricaoMaoDeObra,
    BigDecimal precoTotal,
    OrdemServicoStatus status,
    List<OrdemServicoItemResponse> itens,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static OrdemServicoResponse from(OrdemServico ordemServico) {
    List<OrdemServicoItemResponse> itens =
        ordemServico.getItens().stream().map(OrdemServicoItemResponse::from).toList();

    return new OrdemServicoResponse(
        ordemServico.getId(),
        ordemServico.getCliente().getId(),
        ordemServico.getCliente().getNome(),
        ordemServico.getVeiculo().getId(),
        ordemServico.getVeiculo().getPlaca(),
        ordemServico.getVeiculo().getMarca(),
        ordemServico.getVeiculo().getModelo(),
        ordemServico.getTecnico().getId(),
        ordemServico.getTecnico().getNome(),
        ordemServico.getData(),
        ordemServico.getHora(),
        ordemServico.getKmEntrada(),
        ordemServico.getKmSaida(),
        ordemServico.getCustoServicosTerceirizados(),
        ordemServico.getDescricaoServicosTerceirizados(),
        ordemServico.getCustoPecas(),
        ordemServico.getCustoMaoDeObra(),
        ordemServico.getDescricaoMaoDeObra(),
        ordemServico.getPrecoTotal(),
        ordemServico.getStatus(),
        itens,
        ordemServico.getCriadoEm(),
        ordemServico.getAtualizadoEm());
  }
}

package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServicoItem;
import com.vortex.ordemservico.entity.OrdemServicoItemTipo;
import java.math.BigDecimal;

public record OrdemServicoItemResponse(
    Long id,
    String descricao,
    BigDecimal quantidade,
    BigDecimal valorUnitario,
    OrdemServicoItemTipo tipo,
    BigDecimal valorTotal) {
  public static OrdemServicoItemResponse from(OrdemServicoItem item) {
    BigDecimal valorTotal = item.getQuantidade().multiply(item.getValorUnitario());
    return new OrdemServicoItemResponse(
        item.getId(),
        item.getDescricao(),
        item.getQuantidade(),
        item.getValorUnitario(),
        item.getTipo(),
        valorTotal);
  }
}

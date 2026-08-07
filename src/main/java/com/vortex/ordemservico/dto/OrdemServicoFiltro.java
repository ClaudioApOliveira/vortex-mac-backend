package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServicoStatus;
import java.time.LocalDate;

public record OrdemServicoFiltro(
    OrdemServicoStatus status,
    String busca,
    Long tecnicoId,
    LocalDate dataInicio,
    LocalDate dataFim) {

  public boolean temFiltros() {
    return status != null
        || (busca != null && !busca.isBlank())
        || tecnicoId != null
        || dataInicio != null
        || dataFim != null;
  }
}

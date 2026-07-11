package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServicoStatus;
import com.vortex.ordemservico.entity.OrdemServicoStatusHistorico;
import com.vortex.ordemservico.entity.OrdemServicoStatusHistoricoOrigem;
import java.time.LocalDateTime;

public record OrdemServicoStatusHistoricoResponse(
    Long id,
    OrdemServicoStatus statusAnterior,
    OrdemServicoStatus statusNovo,
    Long usuarioId,
    String usuarioNome,
    OrdemServicoStatusHistoricoOrigem origem,
    String observacao,
    LocalDateTime criadoEm) {

  public static OrdemServicoStatusHistoricoResponse from(OrdemServicoStatusHistorico historico) {
    return new OrdemServicoStatusHistoricoResponse(
        historico.getId(),
        historico.getStatusAnterior(),
        historico.getStatusNovo(),
        historico.getUsuario() != null ? historico.getUsuario().getId() : null,
        historico.getUsuario() != null ? historico.getUsuario().getNome() : null,
        historico.getOrigem(),
        historico.getObservacao(),
        historico.getCriadoEm());
  }
}

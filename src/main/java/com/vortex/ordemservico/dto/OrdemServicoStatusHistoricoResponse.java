package com.vortex.ordemservico.dto;

import com.vortex.ordemservico.entity.OrdemServicoStatus;
import com.vortex.ordemservico.entity.OrdemServicoStatusHistorico;
import com.vortex.ordemservico.entity.OrdemServicoStatusHistoricoOrigem;
import com.vortex.usuario.entity.Usuario;
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
    Usuario usuario = historico.getUsuario();
    return new OrdemServicoStatusHistoricoResponse(
        historico.getId(),
        historico.getStatusAnterior(),
        historico.getStatusNovo(),
        usuario != null ? usuario.getId() : null,
        usuario != null ? usuario.getNome() : null,
        historico.getOrigem(),
        historico.getObservacao(),
        historico.getCriadoEm());
  }
}

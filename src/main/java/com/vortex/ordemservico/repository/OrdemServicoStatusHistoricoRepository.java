package com.vortex.ordemservico.repository;

import com.vortex.ordemservico.entity.OrdemServicoStatusHistorico;
import java.util.List;

public interface OrdemServicoStatusHistoricoRepository {

  OrdemServicoStatusHistorico save(OrdemServicoStatusHistorico historico);

  List<OrdemServicoStatusHistorico> findByOrdemServicoIdOrderByCriadoEmDesc(Long ordemServicoId);

  void limparReferenciaUsuario(Long usuarioId);
}

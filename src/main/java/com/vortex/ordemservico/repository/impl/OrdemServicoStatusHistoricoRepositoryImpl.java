package com.vortex.ordemservico.repository.impl;

import com.vortex.ordemservico.entity.OrdemServicoStatusHistorico;
import com.vortex.ordemservico.repository.OrdemServicoStatusHistoricoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class OrdemServicoStatusHistoricoRepositoryImpl
    implements OrdemServicoStatusHistoricoRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public OrdemServicoStatusHistorico save(OrdemServicoStatusHistorico historico) {
    entityManager.persist(historico);
    return historico;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OrdemServicoStatusHistorico> findByOrdemServicoIdOrderByCriadoEmDesc(
      Long ordemServicoId) {
    return entityManager
        .createQuery(
            """
            SELECT h FROM OrdemServicoStatusHistorico h
            LEFT JOIN FETCH h.usuario
            WHERE h.ordemServico.id = :ordemServicoId
            ORDER BY h.criadoEm DESC, h.id DESC
            """,
            OrdemServicoStatusHistorico.class)
        .setParameter("ordemServicoId", ordemServicoId)
        .getResultList();
  }

  @Override
  public void limparReferenciaUsuario(Long usuarioId) {
    entityManager
        .createNativeQuery(
            "UPDATE ordens_servico_status_historico SET usuario_id = NULL WHERE usuario_id ="
                + " :usuarioId")
        .setParameter("usuarioId", usuarioId)
        .executeUpdate();
  }
}

package com.vortex.ordemservico.repository.impl;

import com.vortex.ordemservico.entity.OrdemServico;
import com.vortex.ordemservico.repository.OrdemServicoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrdemServicoRepositoryImpl implements OrdemServicoRepository {

  private static final String BASE_QUERY =
      """
      SELECT DISTINCT o FROM OrdemServico o
      JOIN FETCH o.cliente
      JOIN FETCH o.veiculo
      JOIN FETCH o.tecnico
      LEFT JOIN FETCH o.itens
      """;

  @PersistenceContext EntityManager entityManager;

  @Override
  public OrdemServico save(OrdemServico ordemServico) {
    if (ordemServico.getId() == null) {
      entityManager.persist(ordemServico);
      return ordemServico;
    }
    return entityManager.merge(ordemServico);
  }

  @Override
  public Optional<OrdemServico> findById(Long id) {
    return entityManager
        .createQuery(BASE_QUERY + " WHERE o.id = :id", OrdemServico.class)
        .setParameter("id", id)
        .getResultStream()
        .findFirst();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OrdemServico> findAll() {
    return entityManager
        .createQuery(
            BASE_QUERY + " ORDER BY o.data DESC, o.hora DESC, o.id DESC", OrdemServico.class)
        .getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OrdemServico> findAllPaginated(int page, int size) {
    return entityManager
        .createQuery(
            BASE_QUERY + " ORDER BY o.data DESC, o.hora DESC, o.id DESC", OrdemServico.class)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  @Override
  public long countAll() {
    return entityManager
        .createQuery("SELECT COUNT(o) FROM OrdemServico o", Long.class)
        .getSingleResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OrdemServico> findByClienteId(Long clienteId) {
    return entityManager
        .createQuery(
            BASE_QUERY
                + " WHERE o.cliente.id = :clienteId ORDER BY o.data DESC, o.hora DESC, o.id DESC",
            OrdemServico.class)
        .setParameter("clienteId", clienteId)
        .getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OrdemServico> findByClienteIdPaginated(Long clienteId, int page, int size) {
    return entityManager
        .createQuery(
            BASE_QUERY
                + " WHERE o.cliente.id = :clienteId ORDER BY o.data DESC, o.hora DESC, o.id DESC",
            OrdemServico.class)
        .setParameter("clienteId", clienteId)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  @Override
  public long countByClienteId(Long clienteId) {
    return entityManager
        .createQuery(
            "SELECT COUNT(o) FROM OrdemServico o WHERE o.cliente.id = :clienteId", Long.class)
        .setParameter("clienteId", clienteId)
        .getSingleResult();
  }

  @Override
  public long countByTecnicoId(Long tecnicoId) {
    return entityManager
        .createQuery(
            "SELECT COUNT(o) FROM OrdemServico o WHERE o.tecnico.id = :tecnicoId", Long.class)
        .setParameter("tecnicoId", tecnicoId)
        .getSingleResult();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OrdemServico> findByVeiculoId(Long veiculoId) {
    return entityManager
        .createQuery(
            BASE_QUERY
                + " WHERE o.veiculo.id = :veiculoId ORDER BY o.data DESC, o.hora DESC, o.id DESC",
            OrdemServico.class)
        .setParameter("veiculoId", veiculoId)
        .getResultList();
  }

  @Override
  public void delete(OrdemServico ordemServico) {
    OrdemServico managed =
        entityManager.contains(ordemServico) ? ordemServico : entityManager.merge(ordemServico);
    entityManager.remove(managed);
  }
}

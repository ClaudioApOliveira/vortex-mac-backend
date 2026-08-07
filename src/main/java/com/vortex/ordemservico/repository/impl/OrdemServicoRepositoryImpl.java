package com.vortex.ordemservico.repository.impl;

import com.vortex.ordemservico.dto.OrdemServicoFiltro;
import com.vortex.ordemservico.entity.OrdemServico;
import com.vortex.ordemservico.repository.OrdemServicoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Override
  public List<OrdemServico> findFiltered(OrdemServicoFiltro filtro, int page, int size) {
    FilterQuery filterQuery = buildFilterQuery(filtro, true);
    TypedQuery<OrdemServico> query =
        entityManager.createQuery(filterQuery.jpql(), OrdemServico.class);
    filterQuery.params().forEach(query::setParameter);
    return query.setFirstResult(page * size).setMaxResults(size).getResultList();
  }

  @Override
  public long countFiltered(OrdemServicoFiltro filtro) {
    FilterQuery filterQuery = buildFilterQuery(filtro, false);
    TypedQuery<Long> query = entityManager.createQuery(filterQuery.jpql(), Long.class);
    filterQuery.params().forEach(query::setParameter);
    return query.getSingleResult();
  }

  private FilterQuery buildFilterQuery(OrdemServicoFiltro filtro, boolean fetch) {
    StringBuilder jpql = new StringBuilder();
    Map<String, Object> params = new HashMap<>();

    if (fetch) {
      jpql.append(BASE_QUERY);
    } else {
      jpql.append("SELECT COUNT(DISTINCT o) FROM OrdemServico o");
    }

    jpql.append(" WHERE 1=1");

    if (filtro.status() != null) {
      jpql.append(" AND o.status = :status");
      params.put("status", filtro.status());
    }

    if (filtro.tecnicoId() != null) {
      jpql.append(" AND o.tecnico.id = :tecnicoId");
      params.put("tecnicoId", filtro.tecnicoId());
    }

    if (filtro.dataInicio() != null) {
      jpql.append(" AND o.data >= :dataInicio");
      params.put("dataInicio", filtro.dataInicio());
    }

    if (filtro.dataFim() != null) {
      jpql.append(" AND o.data <= :dataFim");
      params.put("dataFim", filtro.dataFim());
    }

    if (filtro.busca() != null && !filtro.busca().isBlank()) {
      jpql.append(
          """
           AND (
             LOWER(COALESCE(o.cliente.nome, '')) LIKE :busca
             OR LOWER(COALESCE(o.cliente.razaoSocial, '')) LIKE :busca
             OR LOWER(REPLACE(COALESCE(o.veiculo.placa, ''), '-', '')) LIKE :buscaPlaca
             OR LOWER(COALESCE(o.tecnico.nome, '')) LIKE :busca
           )
          """);
      String termo = filtro.busca().trim().toLowerCase();
      params.put("busca", "%" + termo + "%");
      params.put("buscaPlaca", "%" + termo.replace("-", "").replace(" ", "") + "%");
    }

    if (fetch) {
      jpql.append(" ORDER BY o.data DESC, o.hora DESC, o.id DESC");
    }

    return new FilterQuery(jpql.toString(), params);
  }

  private record FilterQuery(String jpql, Map<String, Object> params) {}

}

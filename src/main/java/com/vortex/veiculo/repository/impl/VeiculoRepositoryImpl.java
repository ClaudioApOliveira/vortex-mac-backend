package com.vortex.veiculo.repository.impl;

import com.vortex.veiculo.entity.Veiculo;
import com.vortex.veiculo.repository.VeiculoQuery;
import com.vortex.veiculo.repository.VeiculoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VeiculoRepositoryImpl implements VeiculoRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Veiculo save(Veiculo veiculo) {
    if (veiculo.getId() == null) {
      entityManager.persist(veiculo);
      return veiculo;
    }
    return entityManager.merge(veiculo);
  }

  @Override
  public Optional<Veiculo> findById(Long id) {
    return entityManager
        .createQuery("SELECT v FROM Veiculo v JOIN FETCH v.cliente WHERE v.id = :id", Veiculo.class)
        .setParameter("id", id)
        .getResultStream()
        .findFirst();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Veiculo> findAll() {
    return entityManager
        .createQuery("SELECT v FROM Veiculo v JOIN FETCH v.cliente ORDER BY v.id", Veiculo.class)
        .getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Veiculo> findByClienteId(Long clienteId) {
    return entityManager
        .createQuery(
            "SELECT v FROM Veiculo v JOIN FETCH v.cliente WHERE v.cliente.id = :clienteId ORDER BY"
                + " v.id",
            Veiculo.class)
        .setParameter("clienteId", clienteId)
        .getResultList();
  }

  @Override
  public void delete(Veiculo veiculo) {
    Veiculo managed = entityManager.contains(veiculo) ? veiculo : entityManager.merge(veiculo);
    entityManager.remove(managed);
  }

  @Override
  public boolean existsByPlaca(String placa) {
    return countByPlaca(placa, null) > 0;
  }

  @Override
  public boolean existsByPlacaAndIdNot(String placa, Long id) {
    return countByPlaca(placa, id) > 0;
  }

  private long countByPlaca(String placa, Long id) {
    VeiculoQuery queryEnum =
        id == null ? VeiculoQuery.CONTAR_POR_PLACA : VeiculoQuery.CONTAR_POR_PLACA_E_ID_DIFERENTE;

    var query = entityManager.createNativeQuery(queryEnum.getSql()).setParameter("placa", placa);

    if (id != null) {
      query.setParameter("id", id);
    }

    return ((Number) query.getSingleResult()).longValue();
  }
}

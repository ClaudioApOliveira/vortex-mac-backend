package com.vortex.cliente.repository.impl;

import com.vortex.cliente.entity.Cliente;
import com.vortex.cliente.repository.ClienteQuery;
import com.vortex.cliente.repository.ClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepositoryImpl implements ClienteRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Cliente save(Cliente cliente) {
    if (cliente.getId() == null) {
      entityManager.persist(cliente);
      return cliente;
    }
    return entityManager.merge(cliente);
  }

  @Override
  public Optional<Cliente> findById(Long id) {
    return entityManager
        .createQuery(
            "SELECT c FROM Cliente c LEFT JOIN FETCH c.endereco LEFT JOIN FETCH c.usuario WHERE"
                + " c.id = :id",
            Cliente.class)
        .setParameter("id", id)
        .getResultStream()
        .findFirst();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Cliente> findAll() {
    return entityManager
        .createQuery(
            "SELECT c FROM Cliente c LEFT JOIN FETCH c.endereco LEFT JOIN FETCH c.usuario",
            Cliente.class)
        .getResultList();
  }

  @Override
  public void delete(Cliente cliente) {
    Cliente managed = entityManager.contains(cliente) ? cliente : entityManager.merge(cliente);
    entityManager.remove(managed);
  }

  @Override
  public Optional<Cliente> findByCpf(String cpf) {
    return entityManager
        .createNativeQuery(ClienteQuery.BUSCAR_POR_CPF.getSql(), Cliente.class)
        .setParameter("cpf", cpf)
        .getResultStream()
        .findFirst();
  }

  @Override
  public boolean existsByCpf(String cpf) {
    return countByCpf(cpf, null) > 0;
  }

  @Override
  public boolean existsByCpfAndIdNot(String cpf, Long id) {
    return countByCpf(cpf, id) > 0;
  }

  @Override
  public boolean existsByCnpj(String cnpj) {
    return countByCnpj(cnpj, null) > 0;
  }

  @Override
  public boolean existsByCnpjAndIdNot(String cnpj, Long id) {
    return countByCnpj(cnpj, id) > 0;
  }

  private long countByCpf(String cpf, Long id) {
    ClienteQuery queryEnum =
        id == null ? ClienteQuery.CONTAR_POR_CPF : ClienteQuery.CONTAR_POR_CPF_E_ID_DIFERENTE;

    var query = entityManager.createNativeQuery(queryEnum.getSql()).setParameter("cpf", cpf);

    if (id != null) {
      query.setParameter("id", id);
    }

    return ((Number) query.getSingleResult()).longValue();
  }

  private long countByCnpj(String cnpj, Long id) {
    ClienteQuery queryEnum =
        id == null ? ClienteQuery.CONTAR_POR_CNPJ : ClienteQuery.CONTAR_POR_CNPJ_E_ID_DIFERENTE;

    var query = entityManager.createNativeQuery(queryEnum.getSql()).setParameter("cnpj", cnpj);

    if (id != null) {
      query.setParameter("id", id);
    }

    return ((Number) query.getSingleResult()).longValue();
  }
}

package com.vortex.endereco.repository.impl;

import com.vortex.endereco.entity.Endereco;
import com.vortex.endereco.repository.EnderecoQuery;
import com.vortex.endereco.repository.EnderecoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class EnderecoRepositoryImpl implements EnderecoRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Endereco save(Endereco endereco) {
    if (endereco.getId() == null) {
      entityManager.persist(endereco);
      return endereco;
    }
    return entityManager.merge(endereco);
  }

  @Override
  public Optional<Endereco> findById(Long id) {
    return entityManager
        .createNativeQuery(EnderecoQuery.BUSCAR_POR_ID.getSql(), Endereco.class)
        .setParameter("id", id)
        .getResultStream()
        .findFirst();
  }

  @Override
  public void delete(Endereco endereco) {
    Endereco managed = entityManager.contains(endereco) ? endereco : entityManager.merge(endereco);
    entityManager.remove(managed);
  }
}

package com.vortex.localidade.repository.impl;

import com.vortex.localidade.entity.Municipio;
import com.vortex.localidade.repository.MunicipioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class MunicipioRepositoryImpl implements MunicipioRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  @SuppressWarnings("unchecked")
  public List<Municipio> findByUf(String uf) {
    return entityManager
        .createQuery("SELECT m FROM Municipio m WHERE m.uf = :uf ORDER BY m.nome", Municipio.class)
        .setParameter("uf", uf.toUpperCase())
        .getResultList();
  }
}

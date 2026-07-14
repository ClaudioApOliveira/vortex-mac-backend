package com.vortex.usuario.repository.impl;

import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioQuery;
import com.vortex.usuario.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioRepositoryImpl implements UsuarioRepository {

  @PersistenceContext EntityManager entityManager;

  @Override
  public Usuario save(Usuario usuario) {
    if (usuario.getId() == null) {
      entityManager.persist(usuario);
      return usuario;
    }
    return entityManager.merge(usuario);
  }

  @Override
  public Optional<Usuario> findById(Long id) {
    return entityManager
        .createNativeQuery(UsuarioQuery.BUSCAR_POR_ID.getSql(), Usuario.class)
        .setParameter("id", id)
        .getResultStream()
        .findFirst();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Usuario> findAll() {
    return entityManager
        .createNativeQuery(UsuarioQuery.LISTAR_TODOS.getSql(), Usuario.class)
        .getResultList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Usuario> findByPerfil(Perfil perfil) {
    return entityManager
        .createNativeQuery(UsuarioQuery.LISTAR_POR_PERFIL.getSql(), Usuario.class)
        .setParameter("perfil", perfil.name())
        .getResultList();
  }

  @Override
  public void delete(Usuario usuario) {
    Usuario managed = entityManager.contains(usuario) ? usuario : entityManager.merge(usuario);
    entityManager.remove(managed);
  }

  @Override
  public Optional<Usuario> findByEmail(String email) {
    return entityManager
        .createNativeQuery(UsuarioQuery.BUSCAR_POR_EMAIL.getSql(), Usuario.class)
        .setParameter("email", email)
        .getResultStream()
        .findFirst();
  }

  @Override
  public boolean existsByEmail(String email) {
    return countByEmail(email, null) > 0;
  }

  @Override
  public boolean existsByEmailAndIdNot(String email, Long id) {
    return countByEmail(email, id) > 0;
  }

  @Override
  public boolean existsByClienteId(Long clienteId) {
    return countByClienteId(clienteId, null) > 0;
  }

  @Override
  public boolean existsByClienteIdAndIdNot(Long clienteId, Long id) {
    return countByClienteId(clienteId, id) > 0;
  }

  @Override
  public long countAdminsAtivos() {
    return ((Number)
            entityManager
                .createNativeQuery(UsuarioQuery.CONTAR_ADMINS_ATIVOS.getSql())
                .getSingleResult())
        .longValue();
  }

  private long countByEmail(String email, Long id) {
    UsuarioQuery queryEnum =
        id == null ? UsuarioQuery.CONTAR_POR_EMAIL : UsuarioQuery.CONTAR_POR_EMAIL_E_ID_DIFERENTE;

    var query = entityManager.createNativeQuery(queryEnum.getSql()).setParameter("email", email);

    if (id != null) {
      query.setParameter("id", id);
    }

    return ((Number) query.getSingleResult()).longValue();
  }

  private long countByClienteId(Long clienteId, Long id) {
    UsuarioQuery queryEnum =
        id == null
            ? UsuarioQuery.CONTAR_POR_CLIENTE_ID
            : UsuarioQuery.CONTAR_POR_CLIENTE_ID_E_ID_DIFERENTE;

    var query =
        entityManager.createNativeQuery(queryEnum.getSql()).setParameter("clienteId", clienteId);

    if (id != null) {
      query.setParameter("id", id);
    }

    return ((Number) query.getSingleResult()).longValue();
  }
}

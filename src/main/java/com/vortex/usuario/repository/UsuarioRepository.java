package com.vortex.usuario.repository;

import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

  Usuario save(Usuario usuario);

  Optional<Usuario> findById(Long id);

  List<Usuario> findAll();

  List<Usuario> findByPerfil(Perfil perfil);

  void delete(Usuario usuario);

  Optional<Usuario> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, Long id);

  boolean existsByClienteId(Long clienteId);

  boolean existsByClienteIdAndIdNot(Long clienteId, Long id);

  long countAdminsAtivos();
}

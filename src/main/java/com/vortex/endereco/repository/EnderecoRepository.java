package com.vortex.endereco.repository;

import com.vortex.endereco.entity.Endereco;
import java.util.Optional;

public interface EnderecoRepository {

  Endereco save(Endereco endereco);

  Optional<Endereco> findById(Long id);

  void delete(Endereco endereco);
}

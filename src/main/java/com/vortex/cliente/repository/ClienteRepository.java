package com.vortex.cliente.repository;

import com.vortex.cliente.entity.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

  Cliente save(Cliente cliente);

  Optional<Cliente> findById(Long id);

  List<Cliente> findAll();

  void delete(Cliente cliente);

  Optional<Cliente> findByCpf(String cpf);

  boolean existsByCpf(String cpf);

  boolean existsByCpfAndIdNot(String cpf, Long id);

  boolean existsByCnpj(String cnpj);

  boolean existsByCnpjAndIdNot(String cnpj, Long id);
}

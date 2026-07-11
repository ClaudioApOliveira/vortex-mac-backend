package com.vortex.veiculo.repository;

import com.vortex.veiculo.entity.Veiculo;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {

  Veiculo save(Veiculo veiculo);

  Optional<Veiculo> findById(Long id);

  List<Veiculo> findAll();

  List<Veiculo> findByClienteId(Long clienteId);

  void delete(Veiculo veiculo);

  boolean existsByPlaca(String placa);

  boolean existsByPlacaAndIdNot(String placa, Long id);
}

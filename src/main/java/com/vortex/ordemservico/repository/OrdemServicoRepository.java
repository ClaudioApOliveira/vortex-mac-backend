package com.vortex.ordemservico.repository;

import com.vortex.ordemservico.entity.OrdemServico;
import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository {

  OrdemServico save(OrdemServico ordemServico);

  Optional<OrdemServico> findById(Long id);

  List<OrdemServico> findAll();

  List<OrdemServico> findAllPaginated(int page, int size);

  long countAll();

  List<OrdemServico> findByClienteId(Long clienteId);

  List<OrdemServico> findByClienteIdPaginated(Long clienteId, int page, int size);

  long countByClienteId(Long clienteId);

  long countByTecnicoId(Long tecnicoId);

  List<OrdemServico> findByVeiculoId(Long veiculoId);

  void delete(OrdemServico ordemServico);
}

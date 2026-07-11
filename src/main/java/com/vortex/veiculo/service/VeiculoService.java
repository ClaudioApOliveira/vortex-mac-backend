package com.vortex.veiculo.service;

import com.vortex.veiculo.dto.VeiculoRequest;
import com.vortex.veiculo.dto.VeiculoResponse;
import java.util.List;

public interface VeiculoService {

  List<VeiculoResponse> listarTodos();

  List<VeiculoResponse> listarPorCliente(Long clienteId);

  VeiculoResponse buscarPorId(Long id);

  VeiculoResponse criar(VeiculoRequest request);

  VeiculoResponse atualizar(Long id, VeiculoRequest request);

  void excluir(Long id);
}

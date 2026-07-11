package com.vortex.cliente.service;

import com.vortex.cliente.dto.ClienteRequest;
import com.vortex.cliente.dto.ClienteResponse;
import java.util.List;

public interface ClienteService {

  List<ClienteResponse> listarTodos();

  ClienteResponse buscarPorId(Long id);

  ClienteResponse criar(ClienteRequest request);

  ClienteResponse atualizar(Long id, ClienteRequest request);

  void excluir(Long id);
}

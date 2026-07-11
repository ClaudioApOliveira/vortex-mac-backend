package com.vortex.ordemservico.service;

import com.vortex.ordemservico.dto.OrdemServicoRequest;
import com.vortex.ordemservico.dto.OrdemServicoResponse;
import com.vortex.ordemservico.dto.OrdemServicoStatusHistoricoResponse;
import com.vortex.shared.response.PageResponse;
import java.util.List;

public interface OrdemServicoService {

  List<OrdemServicoResponse> listarTodos();

  PageResponse<OrdemServicoResponse> listarPaginado(int page, int size);

  List<OrdemServicoResponse> listarPorCliente(Long clienteId);

  List<OrdemServicoResponse> listarPorVeiculo(Long veiculoId);

  OrdemServicoResponse buscarPorId(Long id);

  List<OrdemServicoResponse> listarPorUsuarioCliente(Long usuarioId);

  PageResponse<OrdemServicoResponse> listarPaginadoPorUsuarioCliente(
      Long usuarioId, int page, int size);

  OrdemServicoResponse buscarPorUsuarioCliente(Long usuarioId, Long ordemId);

  OrdemServicoResponse aprovarPorUsuarioCliente(Long usuarioId, Long ordemId);

  OrdemServicoResponse rejeitarPorUsuarioCliente(Long usuarioId, Long ordemId);

  List<OrdemServicoStatusHistoricoResponse> listarHistoricoStatus(Long ordemId);

  List<OrdemServicoStatusHistoricoResponse> listarHistoricoStatusPorUsuarioCliente(
      Long usuarioId, Long ordemId);

  OrdemServicoResponse criar(OrdemServicoRequest request);

  OrdemServicoResponse atualizar(Long id, OrdemServicoRequest request);

  void excluir(Long id);
}

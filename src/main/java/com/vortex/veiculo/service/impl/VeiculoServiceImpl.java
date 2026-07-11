package com.vortex.veiculo.service.impl;

import com.vortex.cliente.entity.Cliente;
import com.vortex.cliente.repository.ClienteRepository;
import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.exception.NotFoundException;
import com.vortex.veiculo.dto.VeiculoRequest;
import com.vortex.veiculo.dto.VeiculoResponse;
import com.vortex.veiculo.entity.Veiculo;
import com.vortex.veiculo.repository.VeiculoRepository;
import com.vortex.veiculo.service.VeiculoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class VeiculoServiceImpl implements VeiculoService {

  private final VeiculoRepository veiculoRepository;
  private final ClienteRepository clienteRepository;

  @Inject
  public VeiculoServiceImpl(
      VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
    this.veiculoRepository = veiculoRepository;
    this.clienteRepository = clienteRepository;
  }

  @Override
  @Transactional
  public List<VeiculoResponse> listarTodos() {
    return veiculoRepository.findAll().stream().map(VeiculoResponse::from).toList();
  }

  @Override
  @Transactional
  public List<VeiculoResponse> listarPorCliente(Long clienteId) {
    validarClienteExistente(clienteId);
    return veiculoRepository.findByClienteId(clienteId).stream()
        .map(VeiculoResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public VeiculoResponse buscarPorId(Long id) {
    Veiculo veiculo = buscarEntidadePorId(id);
    return VeiculoResponse.from(veiculo);
  }

  @Override
  @Transactional
  public VeiculoResponse criar(VeiculoRequest request) {
    validarPlacaUnica(request.placa(), null);

    Veiculo veiculo = new Veiculo();
    aplicarDados(veiculo, request);

    veiculoRepository.save(veiculo);
    return VeiculoResponse.from(veiculo);
  }

  @Override
  @Transactional
  public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
    Veiculo veiculo = buscarEntidadePorId(id);
    validarPlacaUnica(request.placa(), id);
    aplicarDados(veiculo, request);

    return VeiculoResponse.from(veiculo);
  }

  @Override
  @Transactional
  public void excluir(Long id) {
    Veiculo veiculo = buscarEntidadePorId(id);
    veiculoRepository.delete(veiculo);
  }

  private Veiculo buscarEntidadePorId(Long id) {
    return veiculoRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Veículo não encontrado com id: " + id));
  }

  private Cliente validarClienteExistente(Long clienteId) {
    return clienteRepository
        .findById(clienteId)
        .orElseThrow(() -> new NotFoundException("Cliente não encontrado com id: " + clienteId));
  }

  private void validarPlacaUnica(String placa, Long id) {
    String placaNormalizada = normalizarPlaca(placa);

    boolean placaEmUso =
        id == null
            ? veiculoRepository.existsByPlaca(placaNormalizada)
            : veiculoRepository.existsByPlacaAndIdNot(placaNormalizada, id);

    if (placaEmUso) {
      throw new BusinessException("Placa já cadastrada: " + placaNormalizada);
    }
  }

  private void aplicarDados(Veiculo veiculo, VeiculoRequest request) {
    Cliente cliente = validarClienteExistente(request.clienteId());

    veiculo.setCliente(cliente);
    veiculo.setPlaca(normalizarPlaca(request.placa()));
    veiculo.setMarca(request.marca().trim());
    veiculo.setModelo(request.modelo().trim());
    veiculo.setAnoFabricacao(request.anoFabricacao());
    veiculo.setMotor(request.motor());
    veiculo.setCombustivel(request.combustivel());
    veiculo.setKmAtual(request.kmAtual());
  }

  private String normalizarPlaca(String placa) {
    return placa.replaceAll("\\W", "").toUpperCase();
  }
}

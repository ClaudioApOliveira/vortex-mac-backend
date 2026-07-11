package com.vortex.veiculo.dto;

import com.vortex.veiculo.entity.Veiculo;
import java.time.LocalDateTime;

public record VeiculoResponse(
    Long id,
    String placa,
    String marca,
    String modelo,
    Integer anoFabricacao,
    String motor,
    String combustivel,
    Integer kmAtual,
    Long clienteId,
    String clienteNome,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static VeiculoResponse from(Veiculo veiculo) {
    return new VeiculoResponse(
        veiculo.getId(),
        veiculo.getPlaca(),
        veiculo.getMarca(),
        veiculo.getModelo(),
        veiculo.getAnoFabricacao(),
        veiculo.getMotor(),
        veiculo.getCombustivel(),
        veiculo.getKmAtual(),
        veiculo.getCliente().getId(),
        veiculo.getCliente().getNome(),
        veiculo.getCriadoEm(),
        veiculo.getAtualizadoEm());
  }
}

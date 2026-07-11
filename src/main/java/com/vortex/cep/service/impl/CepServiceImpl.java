package com.vortex.cep.service.impl;

import com.vortex.cep.client.OpenCepClient;
import com.vortex.cep.dto.CepResponse;
import com.vortex.cep.dto.OpenCepResponse;
import com.vortex.cep.service.CepService;
import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CepServiceImpl implements CepService {

  private final OpenCepClient openCepClient;

  @Inject
  public CepServiceImpl(OpenCepClient openCepClient) {
    this.openCepClient = openCepClient;
  }

  @Override
  public CepResponse buscarPorCep(String cep) {
    String cepNormalizado = normalizarCep(cep);

    if (cepNormalizado.length() != 8) {
      throw new BusinessException("CEP inválido. Informe 8 dígitos.");
    }

    try {
      OpenCepResponse resposta = openCepClient.buscar(cepNormalizado);
      return CepResponse.from(resposta);
    } catch (WebApplicationException exception) {
      if (exception.getResponse().getStatus() == 404) {
        throw new NotFoundException("CEP não encontrado: " + cepNormalizado);
      }
      throw new BusinessException("Erro ao consultar CEP");
    }
  }

  private String normalizarCep(String cep) {
    if (cep == null) {
      return "";
    }
    return cep.replaceAll("\\D", "");
  }
}

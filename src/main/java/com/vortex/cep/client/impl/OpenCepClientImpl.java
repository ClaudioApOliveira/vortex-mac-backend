package com.vortex.cep.client.impl;

import com.vortex.cep.client.OpenCepClient;
import com.vortex.cep.dto.OpenCepResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OpenCepClientImpl implements OpenCepClient {

  private final OpenCepRestClient openCepRestClient;

  public OpenCepClientImpl(@RestClient OpenCepRestClient openCepRestClient) {
    this.openCepRestClient = openCepRestClient;
  }

  @Override
  public OpenCepResponse buscar(String cep) {
    return openCepRestClient.buscar(cep);
  }
}

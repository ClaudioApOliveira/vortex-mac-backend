package com.vortex.cep.client;

import com.vortex.cep.dto.OpenCepResponse;

public interface OpenCepClient {

  OpenCepResponse buscar(String cep);
}

package com.vortex.cep.service;

import com.vortex.cep.dto.CepResponse;

public interface CepService {

  CepResponse buscarPorCep(String cep);
}

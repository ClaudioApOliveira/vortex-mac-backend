package com.vortex.localidade.service;

import com.vortex.localidade.dto.MunicipioResponse;
import java.util.List;

public interface MunicipioService {

  List<MunicipioResponse> listarPorUf(String uf);
}

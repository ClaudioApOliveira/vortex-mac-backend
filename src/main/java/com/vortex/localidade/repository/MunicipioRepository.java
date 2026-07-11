package com.vortex.localidade.repository;

import com.vortex.localidade.entity.Municipio;
import java.util.List;

public interface MunicipioRepository {

  List<Municipio> findByUf(String uf);
}

package com.vortex.localidade.dto;

import com.vortex.localidade.entity.Municipio;

public record MunicipioResponse(Integer id, String nome, String uf) {
  public static MunicipioResponse from(Municipio municipio) {
    return new MunicipioResponse(municipio.getId(), municipio.getNome(), municipio.getUf());
  }
}

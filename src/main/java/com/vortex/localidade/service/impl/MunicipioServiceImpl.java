package com.vortex.localidade.service.impl;

import com.vortex.localidade.dto.MunicipioResponse;
import com.vortex.localidade.repository.MunicipioRepository;
import com.vortex.localidade.service.MunicipioService;
import com.vortex.shared.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class MunicipioServiceImpl implements MunicipioService {

  private static final Set<String> UFS_VALIDAS =
      Set.of(
          "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB",
          "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

  private final MunicipioRepository municipioRepository;

  @Inject
  public MunicipioServiceImpl(MunicipioRepository municipioRepository) {
    this.municipioRepository = municipioRepository;
  }

  @Override
  @Transactional
  public List<MunicipioResponse> listarPorUf(String uf) {
    String ufNormalizada = normalizarUf(uf);

    return municipioRepository.findByUf(ufNormalizada).stream()
        .map(MunicipioResponse::from)
        .toList();
  }

  private String normalizarUf(String uf) {
    if (uf == null || uf.isBlank()) {
      throw new BusinessException("UF é obrigatória");
    }

    String ufNormalizada = uf.trim().toUpperCase();
    if (ufNormalizada.length() != 2 || !UFS_VALIDAS.contains(ufNormalizada)) {
      throw new BusinessException("UF inválida: " + uf);
    }

    return ufNormalizada;
  }
}

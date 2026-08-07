package com.vortex.ordemservico.service;

import com.vortex.ordemservico.entity.OrdemServicoStatus;
import com.vortex.usuario.entity.Perfil;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class OrdemServicoStatusTransitions {

  private static final Map<OrdemServicoStatus, Set<OrdemServicoStatus>> ALLOWED =
      new EnumMap<>(OrdemServicoStatus.class);

  static {
    ALLOWED.put(
        OrdemServicoStatus.ORCAMENTO,
        Set.copyOf(EnumSet.of(OrdemServicoStatus.APROVADO, OrdemServicoStatus.CANCELADO)));
    ALLOWED.put(
        OrdemServicoStatus.APROVADO,
        Set.copyOf(
            EnumSet.of(
                OrdemServicoStatus.EM_EXECUCAO,
                OrdemServicoStatus.AGUARDANDO_PECAS,
                OrdemServicoStatus.CANCELADO)));
    ALLOWED.put(
        OrdemServicoStatus.EM_EXECUCAO,
        Set.copyOf(
            EnumSet.of(
                OrdemServicoStatus.AGUARDANDO_PECAS,
                OrdemServicoStatus.CONCLUIDO,
                OrdemServicoStatus.CANCELADO)));
    ALLOWED.put(
        OrdemServicoStatus.AGUARDANDO_PECAS,
        Set.copyOf(
            EnumSet.of(
                OrdemServicoStatus.EM_EXECUCAO,
                OrdemServicoStatus.CONCLUIDO,
                OrdemServicoStatus.CANCELADO)));
    ALLOWED.put(OrdemServicoStatus.CONCLUIDO, Set.of());
    ALLOWED.put(OrdemServicoStatus.CANCELADO, Set.copyOf(EnumSet.of(OrdemServicoStatus.ORCAMENTO)));
  }

  private static final Set<OrdemServicoStatus> EMPTY = Set.of();

  private OrdemServicoStatusTransitions() {}

  public static Set<OrdemServicoStatus> proximos(OrdemServicoStatus atual) {
    return ALLOWED.getOrDefault(atual, EMPTY);
  }

  public static boolean podeTransicionar(OrdemServicoStatus atual, OrdemServicoStatus novo) {
    if (atual == null || novo == null || atual == novo) {
      return false;
    }
    return proximos(atual).contains(novo);
  }

  public static boolean podeDefinirStatus(Perfil perfil, OrdemServicoStatus statusSolicitado) {
    if (statusSolicitado == OrdemServicoStatus.APROVADO) {
      return perfil == Perfil.ADMIN || perfil == Perfil.GERENTE;
    }
    return true;
  }
}

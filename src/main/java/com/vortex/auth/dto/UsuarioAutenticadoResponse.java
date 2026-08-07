package com.vortex.auth.dto;

import com.vortex.usuario.entity.Perfil;
import java.time.LocalDateTime;

public record UsuarioAutenticadoResponse(
    Long id,
    String email,
    String nome,
    Perfil perfil,
    Long clienteId,
    boolean deveDefinirSenha,
    LocalDateTime lgpdAceiteEm,
    String lgpdAceiteVersao,
    LocalDateTime lgpdExclusaoSolicitadaEm,
    LocalDateTime lgpdAnonimizadoEm) {}

package com.vortex.auth.dto;

import com.vortex.usuario.entity.Perfil;

public record UsuarioAutenticadoResponse(
    Long id, String email, String nome, Perfil perfil, Long clienteId, boolean deveDefinirSenha) {}

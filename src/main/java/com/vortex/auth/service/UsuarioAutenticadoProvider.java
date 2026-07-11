package com.vortex.auth.service;

import com.vortex.usuario.entity.Usuario;
import java.util.Optional;

public interface UsuarioAutenticadoProvider {

  Optional<Usuario> obterUsuarioAutenticado();

  Usuario obterUsuarioAutenticadoObrigatorio();
}

package com.vortex.usuario.service;

import com.vortex.usuario.dto.UsuarioRequest;
import com.vortex.usuario.dto.UsuarioResponse;
import java.util.List;

public interface UsuarioService {

  List<UsuarioResponse> listarTodos();

  List<UsuarioResponse> listarTecnicos();

  UsuarioResponse buscarPorId(Long id);

  UsuarioResponse criar(UsuarioRequest request);

  UsuarioResponse atualizar(Long id, UsuarioRequest request);

  void excluir(Long id);
}

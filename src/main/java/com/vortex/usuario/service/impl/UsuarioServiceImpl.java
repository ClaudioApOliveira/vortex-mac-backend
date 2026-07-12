package com.vortex.usuario.service.impl;

import com.vortex.auth.security.RefreshTokenService;
import com.vortex.cliente.entity.Cliente;
import com.vortex.cliente.repository.ClienteRepository;
import com.vortex.ordemservico.repository.OrdemServicoRepository;
import com.vortex.ordemservico.repository.OrdemServicoStatusHistoricoRepository;
import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.exception.NotFoundException;
import com.vortex.usuario.dto.UsuarioRequest;
import com.vortex.usuario.dto.UsuarioResponse;
import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import com.vortex.usuario.service.UsuarioService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final ClienteRepository clienteRepository;
  private final RefreshTokenService refreshTokenService;
  private final OrdemServicoRepository ordemServicoRepository;
  private final OrdemServicoStatusHistoricoRepository ordemServicoStatusHistoricoRepository;

  @Inject
  public UsuarioServiceImpl(
      UsuarioRepository usuarioRepository,
      ClienteRepository clienteRepository,
      RefreshTokenService refreshTokenService,
      OrdemServicoRepository ordemServicoRepository,
      OrdemServicoStatusHistoricoRepository ordemServicoStatusHistoricoRepository) {
    this.usuarioRepository = usuarioRepository;
    this.clienteRepository = clienteRepository;
    this.refreshTokenService = refreshTokenService;
    this.ordemServicoRepository = ordemServicoRepository;
    this.ordemServicoStatusHistoricoRepository = ordemServicoStatusHistoricoRepository;
  }

  @Override
  @Transactional
  public List<UsuarioResponse> listarTodos() {
    return usuarioRepository.findAll().stream().map(UsuarioResponse::from).toList();
  }

  @Override
  @Transactional
  public List<UsuarioResponse> listarTecnicos() {
    return usuarioRepository.findByPerfil(Perfil.TECNICO).stream()
        .map(UsuarioResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public UsuarioResponse buscarPorId(Long id) {
    Usuario usuario = buscarEntidadePorId(id);
    return UsuarioResponse.from(usuario);
  }

  @Override
  @Transactional
  public UsuarioResponse criar(UsuarioRequest request) {
    validarSenhaObrigatoria(request.senha(), true);
    validarEmailUnico(request.email(), null);
    validarPerfilCliente(request.perfil(), request.clienteId());

    Usuario usuario = new Usuario();
    usuario.setEmail(request.email());
    usuario.setSenha(BcryptUtil.bcryptHash(request.senha()));
    usuario.setNome(request.nome());
    usuario.setPerfil(request.perfil());
    usuario.setAtivo(request.ativo() == null || request.ativo());
    usuario.setCliente(resolverCliente(request.clienteId(), null));

    usuarioRepository.save(usuario);
    return UsuarioResponse.from(usuario);
  }

  @Override
  @Transactional
  public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
    Usuario usuario = buscarEntidadePorId(id);
    validarSenhaObrigatoria(request.senha(), false);
    validarEmailUnico(request.email(), id);
    validarPerfilCliente(request.perfil(), request.clienteId());

    usuario.setEmail(request.email());
    if (request.senha() != null && !request.senha().isBlank()) {
      usuario.setSenha(BcryptUtil.bcryptHash(request.senha()));
    }
    usuario.setNome(request.nome());
    usuario.setPerfil(request.perfil());
    if (request.ativo() != null) {
      usuario.setAtivo(request.ativo());
    }
    usuario.setCliente(resolverCliente(request.clienteId(), id));

    return UsuarioResponse.from(usuario);
  }

  @Override
  @Transactional
  public void excluir(Long id) {
    Usuario usuario = buscarEntidadePorId(id);
    validarExclusaoSemOrdensServico(id);
    refreshTokenService.removerTodosPorUsuario(id);
    ordemServicoStatusHistoricoRepository.limparReferenciaUsuario(id);
    usuarioRepository.delete(usuario);
  }

  public Usuario buscarEntidadePorId(Long id) {
    return usuarioRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Usuário não encontrado com id: " + id));
  }

  private void validarExclusaoSemOrdensServico(Long usuarioId) {
    if (ordemServicoRepository.countByTecnicoId(usuarioId) > 0) {
      throw new BusinessException(
          "Não é possível excluir o usuário porque ele está vinculado a ordens de serviço como"
              + " técnico");
    }
  }

  private void validarEmailUnico(String email, Long id) {
    boolean emailEmUso =
        id == null
            ? usuarioRepository.existsByEmail(email)
            : usuarioRepository.existsByEmailAndIdNot(email, id);

    if (emailEmUso) {
      throw new BusinessException("Email já cadastrado: " + email);
    }
  }

  private void validarSenhaObrigatoria(String senha, boolean obrigatoria) {
    if (obrigatoria && (senha == null || senha.isBlank())) {
      throw new BusinessException("Senha é obrigatória");
    }
  }

  private void validarPerfilCliente(Perfil perfil, Long clienteId) {
    if (perfil == Perfil.CLIENTE && clienteId == null) {
      throw new BusinessException("Usuário com perfil CLIENTE deve estar vinculado a um cliente");
    }

    if (perfil != Perfil.CLIENTE && clienteId != null) {
      throw new BusinessException(
          "Apenas usuários com perfil CLIENTE podem ser vinculados a um cliente");
    }
  }

  private Cliente resolverCliente(Long clienteId, Long usuarioId) {
    if (clienteId == null) {
      return null;
    }

    Cliente cliente =
        clienteRepository
            .findById(clienteId)
            .orElseThrow(
                () -> new NotFoundException("Cliente não encontrado com id: " + clienteId));

    boolean clienteVinculado =
        usuarioId == null
            ? usuarioRepository.existsByClienteId(clienteId)
            : usuarioRepository.existsByClienteIdAndIdNot(clienteId, usuarioId);

    if (clienteVinculado) {
      throw new BusinessException("Cliente já possui usuário vinculado");
    }

    return cliente;
  }
}

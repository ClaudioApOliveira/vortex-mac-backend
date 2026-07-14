package com.vortex.usuario.service.impl;

import com.vortex.auth.security.RefreshTokenService;
import com.vortex.auth.security.SessaoService;
import com.vortex.auth.service.UsuarioAutenticadoProvider;
import com.vortex.cliente.entity.Cliente;
import com.vortex.ordemservico.repository.OrdemServicoRepository;
import com.vortex.ordemservico.repository.OrdemServicoStatusHistoricoRepository;
import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.exception.NotFoundException;
import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import com.vortex.usuario.service.UsuarioExclusaoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UsuarioExclusaoServiceImpl implements UsuarioExclusaoService {

  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenService refreshTokenService;
  private final SessaoService sessaoService;
  private final OrdemServicoRepository ordemServicoRepository;
  private final OrdemServicoStatusHistoricoRepository ordemServicoStatusHistoricoRepository;
  private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

  @Inject
  public UsuarioExclusaoServiceImpl(
      UsuarioRepository usuarioRepository,
      RefreshTokenService refreshTokenService,
      SessaoService sessaoService,
      OrdemServicoRepository ordemServicoRepository,
      OrdemServicoStatusHistoricoRepository ordemServicoStatusHistoricoRepository,
      UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
    this.usuarioRepository = usuarioRepository;
    this.refreshTokenService = refreshTokenService;
    this.sessaoService = sessaoService;
    this.ordemServicoRepository = ordemServicoRepository;
    this.ordemServicoStatusHistoricoRepository = ordemServicoStatusHistoricoRepository;
    this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
  }

  @Override
  @Transactional
  public void excluir(Long usuarioId) {
    Usuario usuario = buscar(usuarioId);
    validarNaoEhProprioUsuario(usuarioId);
    validarNaoEhUltimoAdmin(usuario);
    validarExclusaoSemOrdensServico(usuarioId);
    limparDependenciasERemover(usuario);
  }

  @Override
  @Transactional
  public void excluirAoRemoverCliente(Long usuarioId) {
    Usuario usuario = buscar(usuarioId);
    validarExclusaoSemOrdensServico(usuarioId);
    limparDependenciasERemover(usuario);
  }

  private Usuario buscar(Long usuarioId) {
    return usuarioRepository
        .findById(usuarioId)
        .orElseThrow(() -> new NotFoundException("Usuário não encontrado com id: " + usuarioId));
  }

  private void limparDependenciasERemover(Usuario usuario) {
    Long usuarioId = usuario.getId();
    sessaoService.invalidarAccessPorUsuario(usuarioId);
    refreshTokenService.removerTodosPorUsuario(usuarioId);
    ordemServicoStatusHistoricoRepository.limparReferenciaUsuario(usuarioId);
    desvincularCliente(usuario);
    usuarioRepository.delete(usuario);
  }

  private void desvincularCliente(Usuario usuario) {
    Cliente cliente = usuario.getCliente();
    if (cliente == null) {
      return;
    }
    cliente.setUsuario(null);
    usuario.setCliente(null);
  }

  private void validarNaoEhProprioUsuario(Long usuarioId) {
    usuarioAutenticadoProvider
        .obterUsuarioAutenticado()
        .ifPresent(
            autenticado -> {
              if (usuarioId.equals(autenticado.getId())) {
                throw new BusinessException("Não é possível excluir o próprio usuário");
              }
            });
  }

  private void validarNaoEhUltimoAdmin(Usuario usuario) {
    if (usuario.getPerfil() != Perfil.ADMIN || !usuario.isAtivo()) {
      return;
    }
    if (usuarioRepository.countAdminsAtivos() <= 1) {
      throw new BusinessException("Não é possível excluir o último administrador ativo");
    }
  }

  private void validarExclusaoSemOrdensServico(Long usuarioId) {
    if (ordemServicoRepository.countByTecnicoId(usuarioId) > 0) {
      throw new BusinessException(
          "Não é possível excluir o usuário porque ele está vinculado a ordens de serviço como"
              + " técnico");
    }
  }
}

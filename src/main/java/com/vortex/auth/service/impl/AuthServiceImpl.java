package com.vortex.auth.service.impl;

import com.vortex.auth.dto.AlterarSenhaRequest;
import com.vortex.auth.dto.AtualizarPerfilRequest;
import com.vortex.auth.dto.LoginRequest;
import com.vortex.auth.dto.LogoutRequest;
import com.vortex.auth.dto.PrimeiroAcessoRequest;
import com.vortex.auth.dto.RefreshTokenRequest;
import com.vortex.auth.dto.TokenResponse;
import com.vortex.auth.dto.UsuarioAutenticadoResponse;
import com.vortex.auth.dto.VerificarPrimeiroAcessoRequest;
import com.vortex.auth.dto.VerificarPrimeiroAcessoResponse;
import com.vortex.auth.entity.RefreshToken;
import com.vortex.auth.security.AccessTokenGerado;
import com.vortex.auth.security.AuthMessages;
import com.vortex.auth.security.JwtClaimsHelper;
import com.vortex.auth.security.JwtService;
import com.vortex.auth.security.RefreshTokenService;
import com.vortex.auth.security.SessaoService;
import com.vortex.auth.service.AuthService;
import com.vortex.cliente.entity.Cliente;
import com.vortex.ordemservico.dto.OrdemServicoResponse;
import com.vortex.ordemservico.dto.OrdemServicoStatusHistoricoResponse;
import com.vortex.ordemservico.service.OrdemServicoService;
import com.vortex.shared.exception.BusinessException;
import com.vortex.shared.exception.UnauthorizedException;
import com.vortex.shared.response.PageResponse;
import com.vortex.usuario.entity.Perfil;
import com.vortex.usuario.entity.Usuario;
import com.vortex.usuario.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

  private final UsuarioRepository usuarioRepository;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final SessaoService sessaoService;
  private final OrdemServicoService ordemServicoService;
  private final JsonWebToken jwt;
  private final SecurityIdentity securityIdentity;

  @Inject
  public AuthServiceImpl(
      UsuarioRepository usuarioRepository,
      JwtService jwtService,
      RefreshTokenService refreshTokenService,
      SessaoService sessaoService,
      OrdemServicoService ordemServicoService,
      JsonWebToken jwt,
      SecurityIdentity securityIdentity) {
    this.usuarioRepository = usuarioRepository;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.sessaoService = sessaoService;
    this.ordemServicoService = ordemServicoService;
    this.jwt = jwt;
    this.securityIdentity = securityIdentity;
  }

  @Override
  @Transactional
  public TokenResponse autenticar(LoginRequest request) {
    Usuario usuario = buscarUsuarioAtivo(request.email(), request.senha());
    return gerarTokens(usuario);
  }

  @Override
  public VerificarPrimeiroAcessoResponse verificarPrimeiroAcesso(
      VerificarPrimeiroAcessoRequest request) {
    String email = normalizarEmail(request.email());
    return usuarioRepository
        .findByEmail(email)
        .filter(this::isElegivelPrimeiroAcesso)
        .map(
            usuario ->
                new VerificarPrimeiroAcessoResponse(usuario.getEmail(), usuario.getNome(), true))
        .orElseGet(() -> new VerificarPrimeiroAcessoResponse(email, null, false));
  }

  @Override
  @Transactional
  public TokenResponse definirSenhaPrimeiroAcesso(PrimeiroAcessoRequest request) {
    if (!request.senha().equals(request.confirmarSenha())) {
      throw new BusinessException("As senhas não conferem");
    }

    Usuario usuario = buscarUsuarioElegivelPrimeiroAcesso(request.email());

    usuario.setSenha(BcryptUtil.bcryptHash(request.senha()));
    usuario.setDeveDefinirSenha(false);

    return gerarTokens(usuario);
  }

  @Override
  @Transactional
  public TokenResponse renovarToken(RefreshTokenRequest request) {
    RefreshToken refreshToken = refreshTokenService.validar(request.refreshToken());

    Usuario usuario = refreshToken.getUsuario();
    if (!usuario.isAtivo()) {
      throw new BusinessException("Usuário inativo");
    }

    refreshTokenService.revogar(request.refreshToken());
    return gerarTokens(usuario);
  }

  @Override
  @Transactional
  public void logout(LogoutRequest request) {
    if (securityIdentity.isAnonymous()) {
      throw new UnauthorizedException("Usuário não autenticado");
    }

    String jti = JwtClaimsHelper.obterJti(jwt);
    if (jti != null) {
      sessaoService.revogarAccess(jti);
    }

    if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
      refreshTokenService.revogar(request.refreshToken());
    }
  }

  @Override
  public UsuarioAutenticadoResponse obterUsuarioAutenticado() {
    return montarUsuarioAutenticadoResponse(obterUsuarioAutenticadoEntidade());
  }

  @Override
  @Transactional
  public UsuarioAutenticadoResponse atualizarPerfil(AtualizarPerfilRequest request) {
    Usuario usuario = obterUsuarioAutenticadoEntidade();
    String email = normalizarEmail(request.email());

    validarEmailUnico(email, usuario.getId());

    usuario.setNome(request.nome().trim());
    usuario.setEmail(email);
    sincronizarDadosCliente(usuario);

    return montarUsuarioAutenticadoResponse(usuario);
  }

  @Override
  @Transactional
  public TokenResponse alterarSenha(AlterarSenhaRequest request) {
    if (!request.novaSenha().equals(request.confirmarSenha())) {
      throw new BusinessException("As senhas não conferem");
    }

    if (request.senhaAtual().equals(request.novaSenha())) {
      throw new BusinessException("A nova senha deve ser diferente da senha atual");
    }

    Usuario usuario = obterUsuarioAutenticadoEntidade();

    if (!usuario.isAtivo()) {
      throw new BusinessException("Usuário inativo");
    }

    if (usuario.isDeveDefinirSenha() || usuario.getSenha() == null) {
      throw new BusinessException("Defina sua senha no primeiro acesso");
    }

    if (!BcryptUtil.matches(request.senhaAtual(), usuario.getSenha())) {
      throw new UnauthorizedException("Senha atual incorreta");
    }

    usuario.setSenha(BcryptUtil.bcryptHash(request.novaSenha()));

    String jti = JwtClaimsHelper.obterJti(jwt);
    if (jti != null) {
      sessaoService.revogarAccess(jti);
    }

    refreshTokenService.revogarTodosPorUsuario(usuario.getId());

    return gerarTokens(usuario);
  }

  @Override
  public List<OrdemServicoResponse> listarMinhasOrdensServico() {
    Usuario usuario = obterUsuarioAutenticadoEntidade();
    return ordemServicoService.listarPorUsuarioCliente(usuario.getId());
  }

  @Override
  public PageResponse<OrdemServicoResponse> listarMinhasOrdensServicoPaginado(int page, int size) {
    Usuario usuario = obterUsuarioAutenticadoEntidade();
    return ordemServicoService.listarPaginadoPorUsuarioCliente(usuario.getId(), page, size);
  }

  @Override
  public OrdemServicoResponse buscarMinhaOrdemServico(Long id) {
    Usuario usuario = obterUsuarioAutenticadoEntidade();
    return ordemServicoService.buscarPorUsuarioCliente(usuario.getId(), id);
  }

  @Override
  public OrdemServicoResponse aprovarMinhaOrdemServico(Long id) {
    Usuario usuario = obterUsuarioAutenticadoEntidade();
    return ordemServicoService.aprovarPorUsuarioCliente(usuario.getId(), id);
  }

  @Override
  public OrdemServicoResponse rejeitarMinhaOrdemServico(Long id) {
    Usuario usuario = obterUsuarioAutenticadoEntidade();
    return ordemServicoService.rejeitarPorUsuarioCliente(usuario.getId(), id);
  }

  @Override
  public List<OrdemServicoStatusHistoricoResponse> listarHistoricoMinhaOrdemServico(Long id) {
    Usuario usuario = obterUsuarioAutenticadoEntidade();
    return ordemServicoService.listarHistoricoStatusPorUsuarioCliente(usuario.getId(), id);
  }

  private UsuarioAutenticadoResponse montarUsuarioAutenticadoResponse(Usuario usuario) {
    Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;

    return new UsuarioAutenticadoResponse(
        usuario.getId(),
        usuario.getEmail(),
        usuario.getNome(),
        usuario.getPerfil(),
        clienteId,
        usuario.isDeveDefinirSenha());
  }

  private Usuario obterUsuarioAutenticadoEntidade() {
    if (securityIdentity.isAnonymous()) {
      throw new UnauthorizedException("Usuário não autenticado");
    }

    long usuarioId = Long.parseLong(jwt.getSubject());
    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

    if (!usuario.isAtivo()) {
      throw new BusinessException("Usuário inativo");
    }

    return usuario;
  }

  private void sincronizarDadosCliente(Usuario usuario) {
    if (usuario.getPerfil() != Perfil.CLIENTE) {
      return;
    }

    Cliente cliente = usuario.getCliente();
    if (cliente != null) {
      cliente.setNome(usuario.getNome());
    }
  }

  private static final String MSG_PRIMEIRO_ACESSO_NEGADO =
      "Não foi possível concluir o primeiro acesso. Verifique os dados informados ou solicite seu"
          + " cadastro à oficina.";

  private void validarEmailUnico(String email, Long usuarioId) {
    boolean emailEmUso = usuarioRepository.existsByEmailAndIdNot(email, usuarioId);
    if (emailEmUso) {
      throw new BusinessException("Email já cadastrado");
    }
  }

  private boolean isElegivelPrimeiroAcesso(Usuario usuario) {
    return usuario.getPerfil() == Perfil.CLIENTE
        && usuario.getCliente() != null
        && usuario.isAtivo()
        && usuario.isDeveDefinirSenha();
  }

  private Usuario buscarUsuarioElegivelPrimeiroAcesso(String email) {
    return usuarioRepository
        .findByEmail(normalizarEmail(email))
        .filter(this::isElegivelPrimeiroAcesso)
        .orElseThrow(() -> new BusinessException(MSG_PRIMEIRO_ACESSO_NEGADO));
  }

  private Usuario buscarUsuarioAtivo(String email, String senha) {
    Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(email)).orElse(null);

    if (usuario == null || !usuario.isAtivo()) {
      throw new UnauthorizedException(AuthMessages.CREDENCIAIS_INVALIDAS);
    }

    if (usuario.isDeveDefinirSenha() || usuario.getSenha() == null) {
      throw new UnauthorizedException(AuthMessages.CREDENCIAIS_INVALIDAS);
    }

    if (!BcryptUtil.matches(senha, usuario.getSenha())) {
      throw new UnauthorizedException(AuthMessages.CREDENCIAIS_INVALIDAS);
    }

    return usuario;
  }

  private TokenResponse gerarTokens(Usuario usuario) {
    Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
    AccessTokenGerado accessTokenGerado =
        jwtService.gerarToken(
            usuario.getId(),
            usuario.getEmail(),
            usuario.getNome(),
            usuario.getPerfil().name(),
            clienteId);

    sessaoService.registrarAccess(
        accessTokenGerado.jti(), usuario.getId(), jwtService.getAccessTokenExpiraEmSegundos());

    String refreshToken = refreshTokenService.criar(usuario.getId());

    return new TokenResponse(
        accessTokenGerado.token(),
        refreshToken,
        "Bearer",
        jwtService.getAccessTokenExpiraEmSegundos(),
        refreshTokenService.getRefreshTokenExpiraEmSegundos(),
        usuario.isDeveDefinirSenha());
  }

  private String normalizarEmail(String email) {
    return email.trim().toLowerCase();
  }
}

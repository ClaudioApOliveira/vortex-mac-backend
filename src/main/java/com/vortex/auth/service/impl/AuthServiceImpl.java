package com.vortex.auth.service.impl;

import com.vortex.auth.dto.AlterarSenhaRequest;
import com.vortex.auth.dto.AtualizarPerfilRequest;
import com.vortex.auth.dto.LoginRequest;
import com.vortex.auth.dto.PrimeiroAcessoRequest;
import com.vortex.auth.dto.TokensGerados;
import com.vortex.auth.dto.UsuarioAutenticadoResponse;
import com.vortex.auth.dto.VerificarPrimeiroAcessoRequest;
import com.vortex.auth.dto.VerificarPrimeiroAcessoResponse;
import com.vortex.auth.entity.RefreshToken;
import com.vortex.auth.security.AuthMessages;
import com.vortex.auth.security.JwtClaimsHelper;
import com.vortex.auth.security.RefreshTokenService;
import com.vortex.auth.security.SessaoService;
import com.vortex.auth.service.AuthEmailRateLimitService;
import com.vortex.auth.service.AuthService;
import com.vortex.auth.service.AuthTokenWriter;
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

  private static final String BCRYPT_DUMMY_HASH =
      "$2a$10$uVthyddDgHbLYpzdkg6uV.i.SITKooF.QgWjsjtczMKNOa620SsYi";

  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenService refreshTokenService;
  private final SessaoService sessaoService;
  private final OrdemServicoService ordemServicoService;
  private final AuthTokenWriter authTokenWriter;
  private final AuthEmailRateLimitService authEmailRateLimitService;
  private final JsonWebToken jwt;
  private final SecurityIdentity securityIdentity;

  @Inject
  public AuthServiceImpl(
      UsuarioRepository usuarioRepository,
      RefreshTokenService refreshTokenService,
      SessaoService sessaoService,
      OrdemServicoService ordemServicoService,
      AuthTokenWriter authTokenWriter,
      AuthEmailRateLimitService authEmailRateLimitService,
      JsonWebToken jwt,
      SecurityIdentity securityIdentity) {
    this.usuarioRepository = usuarioRepository;
    this.refreshTokenService = refreshTokenService;
    this.sessaoService = sessaoService;
    this.ordemServicoService = ordemServicoService;
    this.authTokenWriter = authTokenWriter;
    this.authEmailRateLimitService = authEmailRateLimitService;
    this.jwt = jwt;
    this.securityIdentity = securityIdentity;
  }

  @Override
  public TokensGerados autenticar(LoginRequest request) {
    authEmailRateLimitService.verificarLimite("login", request.email());
    Usuario usuario = buscarUsuarioAtivo(request.email(), request.senha());
    return authTokenWriter.gerarTokens(usuario.getId());
  }

  @Override
  public VerificarPrimeiroAcessoResponse verificarPrimeiroAcesso(
      VerificarPrimeiroAcessoRequest request) {
    authEmailRateLimitService.verificarLimite("verificar-primeiro-acesso", request.email());
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
  public TokensGerados definirSenhaPrimeiroAcesso(PrimeiroAcessoRequest request) {
    authEmailRateLimitService.verificarLimite("primeiro-acesso", request.email());
    if (!request.senha().equals(request.confirmarSenha())) {
      throw new BusinessException("As senhas não conferem");
    }

    Usuario usuario = buscarUsuarioElegivelPrimeiroAcesso(request.email());
    String senhaHash = BcryptUtil.bcryptHash(request.senha());
    return authTokenWriter.definirSenhaEGerarTokens(usuario.getId(), senhaHash);
  }

  @Override
  public TokensGerados renovarToken(String refreshToken) {
    RefreshToken refreshTokenEntidade = refreshTokenService.validarERevogar(refreshToken);

    Usuario usuario = refreshTokenEntidade.getUsuario();
    if (!usuario.isAtivo()) {
      throw new UnauthorizedException(AuthMessages.REFRESH_TOKEN_INVALIDO);
    }

    return authTokenWriter.gerarTokens(usuario.getId());
  }

  @Override
  @Transactional
  public void logout(String refreshToken) {
    if (securityIdentity.isAnonymous()) {
      throw new UnauthorizedException("Usuário não autenticado");
    }

    String jti = JwtClaimsHelper.obterJti(jwt);
    if (jti != null) {
      sessaoService.revogarAccess(jti);
    }

    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenService.revogar(refreshToken);
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
  public TokensGerados alterarSenha(AlterarSenhaRequest request) {
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
      throw new UnauthorizedException(AuthMessages.CREDENCIAIS_INVALIDAS);
    }

    String novaSenhaHash = BcryptUtil.bcryptHash(request.novaSenha());
    String jti = JwtClaimsHelper.obterJti(jwt);
    return authTokenWriter.alterarSenhaEGerarTokens(usuario.getId(), novaSenhaHash, jti);
  }

  @Override
  public List<OrdemServicoResponse> listarMinhasOrdensServico() {
    return ordemServicoService.listarPorUsuarioCliente(obterUsuarioIdAutenticado());
  }

  @Override
  public PageResponse<OrdemServicoResponse> listarMinhasOrdensServicoPaginado(int page, int size) {
    return ordemServicoService.listarPaginadoPorUsuarioCliente(
        obterUsuarioIdAutenticado(), page, size);
  }

  @Override
  public OrdemServicoResponse buscarMinhaOrdemServico(Long id) {
    return ordemServicoService.buscarPorUsuarioCliente(obterUsuarioIdAutenticado(), id);
  }

  @Override
  public OrdemServicoResponse aprovarMinhaOrdemServico(Long id) {
    return ordemServicoService.aprovarPorUsuarioCliente(obterUsuarioIdAutenticado(), id);
  }

  @Override
  public OrdemServicoResponse rejeitarMinhaOrdemServico(Long id) {
    return ordemServicoService.rejeitarPorUsuarioCliente(obterUsuarioIdAutenticado(), id);
  }

  @Override
  public List<OrdemServicoStatusHistoricoResponse> listarHistoricoMinhaOrdemServico(Long id) {
    return ordemServicoService.listarHistoricoStatusPorUsuarioCliente(
        obterUsuarioIdAutenticado(), id);
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

  private long obterUsuarioIdAutenticado() {
    if (securityIdentity.isAnonymous()) {
      throw new UnauthorizedException("Usuário não autenticado");
    }

    return Long.parseLong(jwt.getSubject());
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
    String hashParaComparar =
        usuario != null && usuario.getSenha() != null ? usuario.getSenha() : BCRYPT_DUMMY_HASH;
    boolean senhaCorreta = BcryptUtil.matches(senha, hashParaComparar);

    if (usuario == null
        || !usuario.isAtivo()
        || usuario.isDeveDefinirSenha()
        || usuario.getSenha() == null
        || !senhaCorreta) {
      throw new UnauthorizedException(AuthMessages.CREDENCIAIS_INVALIDAS);
    }

    return usuario;
  }

  private String normalizarEmail(String email) {
    return email.trim().toLowerCase();
  }
}

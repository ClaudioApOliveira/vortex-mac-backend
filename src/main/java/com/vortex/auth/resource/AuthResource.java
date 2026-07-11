package com.vortex.auth.resource;

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
import com.vortex.auth.service.AuthService;
import com.vortex.ordemservico.dto.OrdemServicoResponse;
import com.vortex.ordemservico.dto.OrdemServicoStatusHistoricoResponse;
import com.vortex.shared.openapi.OpenApiConfig;
import com.vortex.shared.response.ApiResponse;
import com.vortex.shared.response.PageResponse;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/auth")
@Tag(name = "Autenticação", description = "Login, refresh, primeiro acesso e sessão do usuário")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

  private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());
  private final AuthService authService;

  @Inject
  public AuthResource(AuthService authService) {
    this.authService = authService;
  }

  @POST
  @Path("/login")
  @PermitAll
  public ApiResponse<TokenResponse> login(@Valid LoginRequest request) {
    TokenResponse response = authService.autenticar(request);
    LOG.info("Login realizado com sucesso");
    return ApiResponse.ok("Login realizado com sucesso", response);
  }

  @POST
  @Path("/refresh")
  @PermitAll
  public ApiResponse<TokenResponse> refresh(@Valid RefreshTokenRequest request) {
    LOG.info("Token renovado com sucesso");
    return ApiResponse.ok("Token renovado com sucesso", authService.renovarToken(request));
  }

  @POST
  @Path("/verificar-primeiro-acesso")
  @PermitAll
  public ApiResponse<VerificarPrimeiroAcessoResponse> verificarPrimeiroAcesso(
      @Valid VerificarPrimeiroAcessoRequest request) {
    LOG.info("Verificando elegibilidade de primeiro acesso");
    return ApiResponse.ok(
        "Email habilitado para primeiro acesso", authService.verificarPrimeiroAcesso(request));
  }

  @POST
  @Path("/primeiro-acesso")
  @PermitAll
  public ApiResponse<TokenResponse> primeiroAcesso(@Valid PrimeiroAcessoRequest request) {
    LOG.info("Senha definida no primeiro acesso");
    return ApiResponse.ok(
        "Senha definida com sucesso", authService.definirSenhaPrimeiroAcesso(request));
  }

  @POST
  @Path("/logout")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<Void> logout(LogoutRequest request) {
    LOG.info("Logout realizado com sucesso");
    authService.logout(request);
    return ApiResponse.ok("Logout realizado com sucesso", null);
  }

  @GET
  @Path("/me")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<UsuarioAutenticadoResponse> me() {
    LOG.info("Obtendo usuário autenticado");
    return ApiResponse.ok(authService.obterUsuarioAutenticado());
  }

  @PUT
  @Path("/me")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<UsuarioAutenticadoResponse> atualizarPerfil(
      @Valid AtualizarPerfilRequest request) {
    LOG.info("Atualizando perfil do usuário autenticado");
    return ApiResponse.ok("Perfil atualizado com sucesso", authService.atualizarPerfil(request));
  }

  @PUT
  @Path("/me/senha")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<TokenResponse> alterarSenha(@Valid AlterarSenhaRequest request) {
    LOG.info("Alterando senha do usuário autenticado");
    return ApiResponse.ok("Senha alterada com sucesso", authService.alterarSenha(request));
  }

  @GET
  @Path("/me/ordens-servico")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<PageResponse<OrdemServicoResponse>> minhasOrdensServico(
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("10") int size) {
    LOG.info("Listando ordens de serviço do usuário autenticado");
    return ApiResponse.ok(authService.listarMinhasOrdensServicoPaginado(page, size));
  }

  @GET
  @Path("/me/ordens-servico/{id}")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<OrdemServicoResponse> minhaOrdemServico(@PathParam("id") Long id) {
    LOG.info("Buscando ordem de serviço do usuário autenticado: " + id);
    return ApiResponse.ok(authService.buscarMinhaOrdemServico(id));
  }

  @POST
  @Path("/me/ordens-servico/{id}/aprovar")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<OrdemServicoResponse> aprovarMinhaOrdemServico(@PathParam("id") Long id) {
    LOG.info("Aprovando orçamento do usuário autenticado: " + id);
    return ApiResponse.ok(
        "Orçamento aprovado com sucesso", authService.aprovarMinhaOrdemServico(id));
  }

  @POST
  @Path("/me/ordens-servico/{id}/rejeitar")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<OrdemServicoResponse> rejeitarMinhaOrdemServico(@PathParam("id") Long id) {
    LOG.info("Rejeitando orçamento do usuário autenticado: " + id);
    return ApiResponse.ok(
        "Orçamento rejeitado com sucesso", authService.rejeitarMinhaOrdemServico(id));
  }

  @GET
  @Path("/me/ordens-servico/{id}/historico-status")
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public ApiResponse<List<OrdemServicoStatusHistoricoResponse>> historicoMinhaOrdemServico(
      @PathParam("id") Long id) {
    LOG.info("Listando histórico de status da ordem do usuário autenticado: " + id);
    return ApiResponse.ok(authService.listarHistoricoMinhaOrdemServico(id));
  }
}

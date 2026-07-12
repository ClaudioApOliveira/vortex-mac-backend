package com.vortex.auth.resource;

import com.vortex.auth.dto.AlterarSenhaRequest;
import com.vortex.auth.dto.AtualizarPerfilRequest;
import com.vortex.auth.dto.LoginRequest;
import com.vortex.auth.dto.PrimeiroAcessoRequest;
import com.vortex.auth.dto.RefreshTokenRequest;
import com.vortex.auth.dto.TokensGerados;
import com.vortex.auth.dto.UsuarioAutenticadoResponse;
import com.vortex.auth.dto.VerificarPrimeiroAcessoRequest;
import com.vortex.auth.dto.VerificarPrimeiroAcessoResponse;
import com.vortex.auth.security.AuthCookieService;
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
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
  private final AuthCookieService authCookieService;

  @Inject
  public AuthResource(AuthService authService, AuthCookieService authCookieService) {
    this.authService = authService;
    this.authCookieService = authCookieService;
  }

  @POST
  @Path("/login")
  @PermitAll
  public Response login(@Valid LoginRequest request) {
    TokensGerados tokens = authService.autenticar(request);
    LOG.info("Login realizado com sucesso");
    return respostaComToken("Login realizado com sucesso", tokens);
  }

  @POST
  @Path("/refresh")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
  @PermitAll
  public Response refresh(
      @CookieParam("refresh_token") String refreshTokenCookie, RefreshTokenRequest request) {
    String refreshToken = resolverRefreshToken(refreshTokenCookie, request);
    LOG.info("Token renovado com sucesso");
    return respostaComToken(
        "Token renovado com sucesso", authService.renovarToken(refreshToken));
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
  public Response primeiroAcesso(@Valid PrimeiroAcessoRequest request) {
    LOG.info("Senha definida no primeiro acesso");
    return respostaComToken(
        "Senha definida com sucesso", authService.definirSenhaPrimeiroAcesso(request));
  }

  @POST
  @Path("/logout")
  @Consumes(MediaType.WILDCARD)
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
  public Response logout(@CookieParam("refresh_token") String refreshTokenCookie) {
    LOG.info("Logout realizado com sucesso");
    authService.logout(refreshTokenCookie);
    return Response.ok(ApiResponse.ok("Logout realizado com sucesso", null))
        .cookie(authCookieService.limparRefreshToken())
        .build();
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
  public Response alterarSenha(@Valid AlterarSenhaRequest request) {
    LOG.info("Alterando senha do usuário autenticado");
    return respostaComToken(
        "Senha alterada com sucesso", authService.alterarSenha(request));
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

  private Response respostaComToken(String message, TokensGerados tokens) {
    return Response.ok(ApiResponse.ok(message, tokens.toResponse()))
        .cookie(
            authCookieService.criarRefreshToken(
                tokens.refreshToken(), tokens.refreshTokenExpiraEmSegundos()))
        .build();
  }

  private String resolverRefreshToken(String refreshTokenCookie, RefreshTokenRequest request) {
    if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
      return refreshTokenCookie;
    }

    if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
      return request.refreshToken();
    }

    return null;
  }
}

package com.vortex.usuario.resource;

import com.vortex.shared.openapi.OpenApiConfig;
import com.vortex.shared.response.ApiResponse;
import com.vortex.usuario.dto.UsuarioRequest;
import com.vortex.usuario.dto.UsuarioResponse;
import com.vortex.usuario.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/usuarios")
@Tag(name = "Usuários", description = "CRUD de usuários internos (ADMIN)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RolesAllowed("ADMIN")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

  private static final Logger LOG = Logger.getLogger(UsuarioResource.class.getName());
  private final UsuarioService usuarioService;

  @Inject
  public UsuarioResource(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GET
  public ApiResponse<List<UsuarioResponse>> listar() {
    LOG.info("Listando todos os usuários");
    return ApiResponse.ok(usuarioService.listarTodos());
  }

  @GET
  @Path("/{id}")
  public ApiResponse<UsuarioResponse> buscar(@PathParam("id") Long id) {
    LOG.info("Buscando usuário por ID: " + id);
    return ApiResponse.ok(usuarioService.buscarPorId(id));
  }

  @POST
  public Response criar(@Valid UsuarioRequest request) {
    UsuarioResponse response = usuarioService.criar(request);
    LOG.info("Usuário criado com id=" + response.id());
    return Response.status(Response.Status.CREATED).entity(ApiResponse.created(response)).build();
  }

  @PUT
  @Path("/{id}")
  public ApiResponse<UsuarioResponse> atualizar(
      @PathParam("id") Long id, @Valid UsuarioRequest request) {
    LOG.info("Atualizando usuário id=" + id);
    return ApiResponse.ok(usuarioService.atualizar(id, request));
  }

  @DELETE
  @Path("/{id}")
  public ApiResponse<Void> excluir(@PathParam("id") Long id) {
    LOG.info("Excluindo usuário: " + id);
    usuarioService.excluir(id);
    return ApiResponse.deleted();
  }
}

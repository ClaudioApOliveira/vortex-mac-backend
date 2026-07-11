package com.vortex.cliente.resource;

import com.vortex.cliente.dto.ClienteRequest;
import com.vortex.cliente.dto.ClienteResponse;
import com.vortex.cliente.service.ClienteService;
import com.vortex.shared.openapi.OpenApiConfig;
import com.vortex.shared.response.ApiResponse;
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

@Path("/api/clientes")
@Tag(name = "Clientes", description = "CRUD de clientes da oficina")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RolesAllowed({"ADMIN", "TECNICO"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

  private static final Logger LOG = Logger.getLogger(ClienteResource.class.getName());
  private final ClienteService clienteService;

  @Inject
  public ClienteResource(ClienteService clienteService) {
    this.clienteService = clienteService;
  }

  @GET
  public ApiResponse<List<ClienteResponse>> listar() {
    LOG.info("Listando todos os clientes");
    return ApiResponse.ok(clienteService.listarTodos());
  }

  @GET
  @Path("/{id}")
  public ApiResponse<ClienteResponse> buscar(@PathParam("id") Long id) {
    LOG.info("Buscando cliente por ID: " + id);
    return ApiResponse.ok(clienteService.buscarPorId(id));
  }

  @POST
  public Response criar(@Valid ClienteRequest request) {
    ClienteResponse response = clienteService.criar(request);
    LOG.info("Cliente criado com id=" + response.id());
    return Response.status(Response.Status.CREATED).entity(ApiResponse.created(response)).build();
  }

  @PUT
  @Path("/{id}")
  public ApiResponse<ClienteResponse> atualizar(
      @PathParam("id") Long id, @Valid ClienteRequest request) {
    LOG.info("Atualizando cliente id=" + id);
    return ApiResponse.ok(clienteService.atualizar(id, request));
  }

  @DELETE
  @Path("/{id}")
  public ApiResponse<Void> excluir(@PathParam("id") Long id) {
    LOG.info("Excluindo cliente: " + id);
    clienteService.excluir(id);
    return ApiResponse.deleted();
  }
}

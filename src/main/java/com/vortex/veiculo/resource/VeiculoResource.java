package com.vortex.veiculo.resource;

import com.vortex.shared.openapi.OpenApiConfig;
import com.vortex.shared.response.ApiResponse;
import com.vortex.veiculo.dto.VeiculoRequest;
import com.vortex.veiculo.dto.VeiculoResponse;
import com.vortex.veiculo.service.VeiculoService;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/veiculos")
@Tag(name = "Veículos", description = "CRUD de veículos vinculados aos clientes")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RolesAllowed({"ADMIN", "TECNICO"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VeiculoResource {

  private final VeiculoService veiculoService;

  @Inject
  public VeiculoResource(VeiculoService veiculoService) {
    this.veiculoService = veiculoService;
  }

  @GET
  public ApiResponse<List<VeiculoResponse>> listar() {
    return ApiResponse.ok(veiculoService.listarTodos());
  }

  @GET
  @Path("/cliente/{clienteId}")
  public ApiResponse<List<VeiculoResponse>> listarPorCliente(
      @PathParam("clienteId") Long clienteId) {
    return ApiResponse.ok(veiculoService.listarPorCliente(clienteId));
  }

  @GET
  @Path("/{id}")
  public ApiResponse<VeiculoResponse> buscar(@PathParam("id") Long id) {
    return ApiResponse.ok(veiculoService.buscarPorId(id));
  }

  @POST
  public Response criar(@Valid VeiculoRequest request) {
    VeiculoResponse response = veiculoService.criar(request);
    return Response.status(Response.Status.CREATED).entity(ApiResponse.created(response)).build();
  }

  @PUT
  @Path("/{id}")
  public ApiResponse<VeiculoResponse> atualizar(
      @PathParam("id") Long id, @Valid VeiculoRequest request) {
    return ApiResponse.ok(veiculoService.atualizar(id, request));
  }

  @DELETE
  @Path("/{id}")
  public ApiResponse<Void> excluir(@PathParam("id") Long id) {
    veiculoService.excluir(id);
    return ApiResponse.deleted();
  }
}

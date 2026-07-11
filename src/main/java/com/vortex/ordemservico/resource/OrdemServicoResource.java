package com.vortex.ordemservico.resource;

import com.vortex.ordemservico.dto.OrdemServicoRequest;
import com.vortex.ordemservico.dto.OrdemServicoResponse;
import com.vortex.ordemservico.dto.OrdemServicoStatusHistoricoResponse;
import com.vortex.ordemservico.service.OrdemServicoService;
import com.vortex.shared.openapi.OpenApiConfig;
import com.vortex.shared.response.ApiResponse;
import com.vortex.shared.response.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/ordens-servico")
@Tag(name = "Ordens de Serviço", description = "CRUD de ordens de serviço com peças e serviços")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RolesAllowed({"ADMIN", "TECNICO"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdemServicoResource {

  private final OrdemServicoService ordemServicoService;

  @Inject
  public OrdemServicoResource(OrdemServicoService ordemServicoService) {
    this.ordemServicoService = ordemServicoService;
  }

  @GET
  public ApiResponse<PageResponse<OrdemServicoResponse>> listar(
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("10") int size) {
    return ApiResponse.ok(ordemServicoService.listarPaginado(page, size));
  }

  @GET
  @Path("/cliente/{clienteId}")
  public ApiResponse<List<OrdemServicoResponse>> listarPorCliente(
      @PathParam("clienteId") Long clienteId) {
    return ApiResponse.ok(ordemServicoService.listarPorCliente(clienteId));
  }

  @GET
  @Path("/veiculo/{veiculoId}")
  public ApiResponse<List<OrdemServicoResponse>> listarPorVeiculo(
      @PathParam("veiculoId") Long veiculoId) {
    return ApiResponse.ok(ordemServicoService.listarPorVeiculo(veiculoId));
  }

  @GET
  @Path("/{id}")
  public ApiResponse<OrdemServicoResponse> buscar(@PathParam("id") Long id) {
    return ApiResponse.ok(ordemServicoService.buscarPorId(id));
  }

  @GET
  @Path("/{id}/historico-status")
  public ApiResponse<List<OrdemServicoStatusHistoricoResponse>> listarHistoricoStatus(
      @PathParam("id") Long id) {
    return ApiResponse.ok(ordemServicoService.listarHistoricoStatus(id));
  }

  @POST
  public Response criar(@Valid OrdemServicoRequest request) {
    OrdemServicoResponse response = ordemServicoService.criar(request);
    return Response.status(Response.Status.CREATED).entity(ApiResponse.created(response)).build();
  }

  @PUT
  @Path("/{id}")
  public ApiResponse<OrdemServicoResponse> atualizar(
      @PathParam("id") Long id, @Valid OrdemServicoRequest request) {
    return ApiResponse.ok(ordemServicoService.atualizar(id, request));
  }

  @DELETE
  @Path("/{id}")
  public ApiResponse<Void> excluir(@PathParam("id") Long id) {
    ordemServicoService.excluir(id);
    return ApiResponse.deleted();
  }
}

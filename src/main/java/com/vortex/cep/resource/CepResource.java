package com.vortex.cep.resource;

import com.vortex.cep.dto.CepResponse;
import com.vortex.cep.service.CepService;
import com.vortex.shared.openapi.OpenApiConfig;
import com.vortex.shared.response.ApiResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/ceps")
@Tag(name = "CEP", description = "Consulta de endereço por CEP via OpenCEP")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class CepResource {

  private final CepService cepService;

  @Inject
  public CepResource(CepService cepService) {
    this.cepService = cepService;
  }

  @GET
  @Path("/{cep}")
  public ApiResponse<CepResponse> buscar(@PathParam("cep") String cep) {
    return ApiResponse.ok(cepService.buscarPorCep(cep));
  }
}

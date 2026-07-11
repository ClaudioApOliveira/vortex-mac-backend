package com.vortex.localidade.resource;

import com.vortex.localidade.dto.MunicipioResponse;
import com.vortex.localidade.service.MunicipioService;
import com.vortex.shared.response.ApiResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/localidades/estados")
@Tag(name = "Localidades", description = "Consulta de municípios por UF (público)")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
public class MunicipioResource {

  private final MunicipioService municipioService;

  @Inject
  public MunicipioResource(MunicipioService municipioService) {
    this.municipioService = municipioService;
  }

  @GET
  @Path("/{uf}/municipios")
  public ApiResponse<List<MunicipioResponse>> listarPorUf(@PathParam("uf") String uf) {
    return ApiResponse.ok(municipioService.listarPorUf(uf));
  }
}

package com.vortex.usuario.resource;

import com.vortex.shared.openapi.OpenApiConfig;
import com.vortex.shared.response.ApiResponse;
import com.vortex.usuario.dto.UsuarioResponse;
import com.vortex.usuario.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/tecnicos")
@Tag(name = "Técnicos", description = "Listagem de técnicos disponíveis para ordens de serviço")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RolesAllowed({"ADMIN", "TECNICO"})
@Produces(MediaType.APPLICATION_JSON)
public class TecnicoResource {

  private final UsuarioService usuarioService;

  @Inject
  public TecnicoResource(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GET
  public ApiResponse<List<UsuarioResponse>> listar() {
    return ApiResponse.ok(usuarioService.listarTecnicos());
  }
}

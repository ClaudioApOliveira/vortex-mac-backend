package com.vortex.cep.client.impl;

import com.vortex.cep.dto.OpenCepResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "opencep-api")
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface OpenCepRestClient {

  @GET
  @Path("/{cep}")
  OpenCepResponse buscar(@PathParam("cep") String cep);
}

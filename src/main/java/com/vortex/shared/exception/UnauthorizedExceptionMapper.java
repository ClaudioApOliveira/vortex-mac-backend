package com.vortex.shared.exception;

import com.vortex.shared.response.ErrorResponse;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

  @Override
  public Response toResponse(UnauthorizedException exception) {
    int status = Response.Status.UNAUTHORIZED.getStatusCode();
    String message = exception.getMessage() != null ? exception.getMessage() : "Não autenticado";

    return Response.status(status).entity(ErrorResponse.of(status, message)).build();
  }
}

package com.vortex.shared.exception;

import com.vortex.shared.response.ErrorResponse;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

  @Override
  public Response toResponse(ForbiddenException exception) {
    int status = Response.Status.FORBIDDEN.getStatusCode();
    return Response.status(status).entity(ErrorResponse.of(status, "Acesso negado")).build();
  }
}

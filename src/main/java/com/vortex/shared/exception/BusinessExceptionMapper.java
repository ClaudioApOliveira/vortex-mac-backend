package com.vortex.shared.exception;

import com.vortex.shared.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

  @Override
  public Response toResponse(BusinessException exception) {
    int status =
        switch (exception) {
          case NotFoundException notFound -> Response.Status.NOT_FOUND.getStatusCode();
          case UnauthorizedException unauthorized -> Response.Status.UNAUTHORIZED.getStatusCode();
          default -> Response.Status.BAD_REQUEST.getStatusCode();
        };

    return Response.status(status).entity(ErrorResponse.of(status, exception.getMessage())).build();
  }
}

package com.vortex.shared.exception;

import com.vortex.shared.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<RuntimeException> {

  private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

  @Override
  public Response toResponse(RuntimeException exception) {
    if (exception instanceof WebApplicationException webException) {
      int status = webException.getResponse().getStatus();
      String message =
          webException.getMessage() != null
              ? webException.getMessage()
              : Response.Status.fromStatusCode(status).getReasonPhrase();

      return Response.status(status).entity(ErrorResponse.of(status, message)).build();
    }

    LOG.error("Erro interno não tratado", exception);

    int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    return Response.status(status)
        .entity(ErrorResponse.of(status, "Erro interno do servidor"))
        .build();
  }
}

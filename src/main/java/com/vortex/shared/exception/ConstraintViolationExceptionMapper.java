package com.vortex.shared.exception;

import com.vortex.shared.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    List<String> errors =
        exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList();

    String message =
        errors.size() == 1 ? errors.getFirst() : "Erro de validação nos campos informados";

    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ErrorResponse.of(Response.Status.BAD_REQUEST.getStatusCode(), message, errors))
        .build();
  }
}

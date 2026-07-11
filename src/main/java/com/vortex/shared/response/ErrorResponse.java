package com.vortex.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    boolean success, int status, String message, List<String> errors, LocalDateTime timestamp) {

  public static ErrorResponse of(int status, String message) {
    return new ErrorResponse(false, status, message, null, LocalDateTime.now());
  }

  public static ErrorResponse of(int status, String message, List<String> errors) {
    return new ErrorResponse(false, status, message, errors, LocalDateTime.now());
  }
}

package com.vortex.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success, int status, String message, T data, LocalDateTime timestamp) {

  public static <T> ApiResponse<T> of(int status, String message, T data) {
    return new ApiResponse<>(true, status, message, data, LocalDateTime.now());
  }

  public static <T> ApiResponse<T> ok(T data) {
    return of(200, "Operação realizada com sucesso", data);
  }

  public static <T> ApiResponse<T> ok(String message, T data) {
    return of(200, message, data);
  }

  public static ApiResponse<Void> ok() {
    return of(200, "Operação realizada com sucesso", null);
  }

  public static <T> ApiResponse<T> created(T data) {
    return of(201, "Recurso criado com sucesso", data);
  }

  public static ApiResponse<Void> deleted() {
    return of(200, "Recurso excluído com sucesso", null);
  }
}

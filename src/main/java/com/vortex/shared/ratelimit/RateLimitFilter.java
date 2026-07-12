package com.vortex.shared.ratelimit;

import com.vortex.shared.response.ErrorResponse;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class RateLimitFilter implements ContainerRequestFilter {

  private static final String PREFIXO_CHAVE = "mec:ratelimit:";
  private static final Set<String> ROTAS_AUTH =
      Set.of(
          "/api/auth/login",
          "/api/auth/refresh",
          "/api/auth/verificar-primeiro-acesso",
          "/api/auth/primeiro-acesso");

  private final RateLimitService rateLimitService;
  private final RateLimitConfig rateLimitConfig;
  private final CurrentVertxRequest currentVertxRequest;

  @Inject
  public RateLimitFilter(
      RateLimitService rateLimitService,
      RateLimitConfig rateLimitConfig,
      CurrentVertxRequest currentVertxRequest) {
    this.rateLimitService = rateLimitService;
    this.rateLimitConfig = rateLimitConfig;
    this.currentVertxRequest = currentVertxRequest;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!rateLimitConfig.isEnabled()) {
      return;
    }

    String caminho = normalizarCaminho(requestContext.getUriInfo().getPath());
    if (caminho.startsWith("/q/")) {
      return;
    }

    String ip = obterIp(requestContext);
    boolean permitido;

    if (ROTAS_AUTH.contains(caminho)) {
      String chave = PREFIXO_CHAVE + "auth:" + ip + ":" + caminho;
      permitido =
          rateLimitService.tentarConsumir(
              chave, rateLimitConfig.getAuthRequests(), rateLimitConfig.getAuthWindowSeconds());
    } else if (caminho.startsWith("/api/")) {
      String chave = PREFIXO_CHAVE + "api:" + ip;
      permitido =
          rateLimitService.tentarConsumir(
              chave, rateLimitConfig.getApiRequests(), rateLimitConfig.getApiWindowSeconds());
    } else {
      return;
    }

    if (!permitido) {
      requestContext.abortWith(
          Response.status(Response.Status.TOO_MANY_REQUESTS)
              .entity(
                  ErrorResponse.of(
                      Response.Status.TOO_MANY_REQUESTS.getStatusCode(),
                      "Muitas requisições. Tente novamente em alguns instantes."))
              .build());
    }
  }

  private String normalizarCaminho(String caminho) {
    if (caminho == null || caminho.isBlank()) {
      return "/";
    }
    return caminho.startsWith("/") ? caminho : "/" + caminho;
  }

  private String obterIp(ContainerRequestContext requestContext) {
    if (rateLimitConfig.isTrustProxy()) {
      String encaminhado = requestContext.getHeaderString("X-Forwarded-For");
      if (encaminhado != null && !encaminhado.isBlank()) {
        return encaminhado.split(",")[0].trim();
      }

      String ipReal = requestContext.getHeaderString("X-Real-IP");
      if (ipReal != null && !ipReal.isBlank()) {
        return ipReal.trim();
      }
    }

    if (currentVertxRequest.getCurrent() != null
        && currentVertxRequest.getCurrent().request() != null
        && currentVertxRequest.getCurrent().request().remoteAddress() != null) {
      return currentVertxRequest.getCurrent().request().remoteAddress().host();
    }

    return "desconhecido";
  }
}

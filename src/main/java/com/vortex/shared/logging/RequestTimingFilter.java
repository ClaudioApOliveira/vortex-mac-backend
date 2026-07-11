package com.vortex.shared.logging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

@ApplicationScoped
public class RequestTimingFilter {

  static final String PROP_INICIO = "vortex.request.inicio";

  private static final Logger LOG = Logger.getLogger(RequestTimingFilter.class);

  private final RequestTimingConfig requestTimingConfig;

  @Inject
  public RequestTimingFilter(RequestTimingConfig requestTimingConfig) {
    this.requestTimingConfig = requestTimingConfig;
  }

  @ServerRequestFilter
  public void iniciar(ContainerRequestContext requestContext) {
    if (!requestTimingConfig.isEnabled() || deveIgnorar(requestContext)) {
      return;
    }

    requestContext.setProperty(PROP_INICIO, System.nanoTime());
  }

  @ServerResponseFilter
  public void finalizar(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (!requestTimingConfig.isEnabled() || deveIgnorar(requestContext)) {
      return;
    }

    Object inicio = requestContext.getProperty(PROP_INICIO);
    if (!(inicio instanceof Long inicioNanos)) {
      return;
    }

    long duracaoMs = (System.nanoTime() - inicioNanos) / 1_000_000;
    String metodo = requestContext.getMethod();
    String caminho = normalizarCaminho(requestContext.getUriInfo().getPath());
    int status = responseContext.getStatus();

    LOG.infof("%s %s - %d - %dms", metodo, caminho, status, duracaoMs);
  }

  private boolean deveIgnorar(ContainerRequestContext requestContext) {
    return "OPTIONS".equalsIgnoreCase(requestContext.getMethod())
        || normalizarCaminho(requestContext.getUriInfo().getPath()).startsWith("/q/");
  }

  private String normalizarCaminho(String caminho) {
    if (caminho == null || caminho.isBlank()) {
      return "/";
    }
    return caminho.startsWith("/") ? caminho : "/" + caminho;
  }
}

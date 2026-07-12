package com.vortex.auth.security.filter;

import com.vortex.auth.security.AuthCookieService;
import com.vortex.auth.security.AuthCsrfService;
import com.vortex.shared.response.ErrorResponse;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION - 50)
public class AuthCsrfFilter implements ContainerRequestFilter {

  private static final Set<String> ROTAS_PROTEGIDAS =
      Set.of("/api/auth/refresh", "/api/auth/logout");

  private final AuthCsrfService authCsrfService;
  private final AuthCookieService authCookieService;

  @Inject
  public AuthCsrfFilter(AuthCsrfService authCsrfService, AuthCookieService authCookieService) {
    this.authCsrfService = authCsrfService;
    this.authCookieService = authCookieService;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!authCsrfService.isEnabled()) {
      return;
    }

    String caminho = normalizarCaminho(requestContext.getUriInfo().getPath());
    if (!ROTAS_PROTEGIDAS.contains(caminho)
        || !"POST".equalsIgnoreCase(requestContext.getMethod())) {
      return;
    }

    Map<String, Cookie> cookies = requestContext.getCookies();
    Cookie refreshCookie = cookies.get(authCookieService.getCookieName());
    if (refreshCookie == null
        || refreshCookie.getValue() == null
        || refreshCookie.getValue().isBlank()) {
      return;
    }

    Cookie csrfCookie = cookies.get(authCsrfService.getCookieName());
    String csrfCookieValor = csrfCookie != null ? csrfCookie.getValue() : null;
    String csrfHeader = requestContext.getHeaderString(authCsrfService.getHeaderName());

    try {
      authCsrfService.validar(csrfCookieValor, csrfHeader);
    } catch (com.vortex.shared.exception.UnauthorizedException exception) {
      requestContext.abortWith(
          Response.status(Response.Status.FORBIDDEN)
              .entity(
                  ErrorResponse.of(
                      Response.Status.FORBIDDEN.getStatusCode(), exception.getMessage()))
              .build());
    }
  }

  private String normalizarCaminho(String caminho) {
    if (caminho == null || caminho.isBlank()) {
      return "/";
    }
    return caminho.startsWith("/") ? caminho : "/" + caminho;
  }
}

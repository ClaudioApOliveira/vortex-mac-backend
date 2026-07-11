package com.vortex.auth.security.filter;

import com.vortex.auth.security.JwtClaimsHelper;
import com.vortex.auth.security.SessaoService;
import com.vortex.shared.response.ErrorResponse;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SessaoValkeyFilter implements ContainerRequestFilter {

  private final SessaoService sessaoService;
  private final SecurityIdentity securityIdentity;
  private final Instance<JsonWebToken> jwt;

  @Inject
  public SessaoValkeyFilter(
      SessaoService sessaoService, SecurityIdentity securityIdentity, Instance<JsonWebToken> jwt) {
    this.sessaoService = sessaoService;
    this.securityIdentity = securityIdentity;
    this.jwt = jwt;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (securityIdentity.isAnonymous() || !jwt.isResolvable()) {
      return;
    }

    String jti = JwtClaimsHelper.obterJti(jwt.get());
    if (jti == null || jti.isBlank()) {
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(
                  ErrorResponse.of(Response.Status.UNAUTHORIZED.getStatusCode(), "Token inválido"))
              .build());
      return;
    }

    if (sessaoService.accessAtivo(jti)) {
      return;
    }

    requestContext.abortWith(
        Response.status(Response.Status.UNAUTHORIZED)
            .entity(
                ErrorResponse.of(
                    Response.Status.UNAUTHORIZED.getStatusCode(), "Sessão inválida ou expirada"))
            .build());
  }
}

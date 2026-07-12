package com.vortex.auth.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthCookieService {

  @ConfigProperty(name = "vortex.auth.refresh-token.cookie-name", defaultValue = "refresh_token")
  String cookieName;

  @ConfigProperty(name = "vortex.auth.cookie.path", defaultValue = "/api/auth")
  String path;

  @ConfigProperty(name = "vortex.auth.cookie.secure", defaultValue = "false")
  boolean secure;

  @ConfigProperty(name = "vortex.auth.cookie.same-site", defaultValue = "Lax")
  String sameSite;

  public NewCookie criarRefreshToken(String token, long maxAgeSegundos) {
    return baseBuilder(token).maxAge((int) maxAgeSegundos).build();
  }

  public NewCookie limparRefreshToken() {
    return baseBuilder("").maxAge(0).build();
  }

  public String getCookieName() {
    return cookieName;
  }

  private NewCookie.Builder baseBuilder(String value) {
    return new NewCookie.Builder(cookieName)
        .value(value)
        .path(path)
        .httpOnly(true)
        .secure(secure)
        .sameSite(NewCookie.SameSite.valueOf(sameSite.toUpperCase()));
  }
}

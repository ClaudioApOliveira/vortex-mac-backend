package com.vortex.auth.security;

import com.vortex.shared.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthCsrfService {

  /**
   * Lazy: beans Arc são materializados no build nativo; criar SecureRandom no campo
   * coloca seed no image heap e o GraalVM rejeita.
   */
  private volatile SecureRandom secureRandom;

  @ConfigProperty(name = "vortex.auth.csrf.enabled", defaultValue = "true")
  boolean enabled;

  @ConfigProperty(name = "vortex.auth.csrf.cookie-name", defaultValue = "csrf_token")
  String cookieName;

  @ConfigProperty(name = "vortex.auth.csrf.header-name", defaultValue = "X-CSRF-Token")
  String headerName;

  @ConfigProperty(name = "vortex.auth.csrf.cookie-path", defaultValue = "/")
  String path;

  @ConfigProperty(name = "vortex.auth.cookie.secure", defaultValue = "false")
  boolean secure;

  @ConfigProperty(name = "vortex.auth.cookie.same-site", defaultValue = "Lax")
  String sameSite;

  public boolean isEnabled() {
    return enabled;
  }

  public String getCookieName() {
    return cookieName;
  }

  public String getHeaderName() {
    return headerName;
  }

  public String gerarToken() {
    byte[] bytes = new byte[32];
    secureRandom().nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }

  private SecureRandom secureRandom() {
    SecureRandom local = secureRandom;
    if (local == null) {
      synchronized (this) {
        local = secureRandom;
        if (local == null) {
          secureRandom = local = new SecureRandom();
        }
      }
    }
    return local;
  }

  public NewCookie criarCookie(String token) {
    return new NewCookie.Builder(cookieName)
        .value(token)
        .path(path)
        .httpOnly(false)
        .secure(secure)
        .sameSite(NewCookie.SameSite.valueOf(sameSite.toUpperCase()))
        .build();
  }

  public NewCookie limparCookie() {
    return new NewCookie.Builder(cookieName)
        .value("")
        .path(path)
        .httpOnly(false)
        .secure(secure)
        .sameSite(NewCookie.SameSite.valueOf(sameSite.toUpperCase()))
        .maxAge(0)
        .build();
  }

  public void validar(String cookieToken, String headerToken) {
    if (!enabled) {
      return;
    }

    if (cookieToken == null
        || cookieToken.isBlank()
        || headerToken == null
        || headerToken.isBlank()) {
      throw new UnauthorizedException("Token CSRF inválido ou ausente");
    }

    byte[] cookieBytes = cookieToken.getBytes();
    byte[] headerBytes = headerToken.getBytes();
    if (!MessageDigest.isEqual(cookieBytes, headerBytes)) {
      throw new UnauthorizedException("Token CSRF inválido ou ausente");
    }
  }
}

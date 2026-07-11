package com.vortex.auth.security;

import org.eclipse.microprofile.jwt.JsonWebToken;

public final class JwtClaimsHelper {

  private JwtClaimsHelper() {}

  public static String obterJti(JsonWebToken jwt) {
    if (jwt == null) {
      return null;
    }

    String jti = jwt.getTokenID();
    if (jti != null && !jti.isBlank()) {
      return jti;
    }

    return jwt.claim("jti").map(String::valueOf).filter(valor -> !valor.isBlank()).orElse(null);
  }
}

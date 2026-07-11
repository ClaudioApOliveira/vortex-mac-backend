package com.vortex.auth.security.impl;

import com.vortex.auth.security.AccessTokenGerado;
import com.vortex.auth.security.JwtService;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JwtServiceImpl implements JwtService {

  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String issuer;

  @ConfigProperty(name = "vortex.jwt.access-token.lifespan", defaultValue = "900")
  long accessTokenExpiraEmSegundos;

  @Override
  public AccessTokenGerado gerarToken(
      Long usuarioId, String email, String nome, String perfil, Long clienteId) {
    String jti = UUID.randomUUID().toString();

    var builder =
        Jwt.issuer(issuer)
            .upn(email)
            .subject(String.valueOf(usuarioId))
            .claim("jti", jti)
            .groups(Set.of(perfil))
            .claim("nome", nome)
            .expiresIn(Duration.ofSeconds(accessTokenExpiraEmSegundos));

    if (clienteId != null) {
      builder.claim("clienteId", clienteId);
    }

    return new AccessTokenGerado(builder.sign(), jti);
  }

  @Override
  public long getAccessTokenExpiraEmSegundos() {
    return accessTokenExpiraEmSegundos;
  }
}

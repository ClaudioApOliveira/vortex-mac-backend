package com.vortex.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class TokenHashUtil {

  private static final ThreadLocal<MessageDigest> SHA256 =
      ThreadLocal.withInitial(
          () -> {
            try {
              return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException exception) {
              throw new IllegalStateException("Algoritmo SHA-256 não disponível", exception);
            }
          });

  private TokenHashUtil() {}

  public static String hash(String token) {
    MessageDigest digest = SHA256.get();
    digest.reset();
    byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(hash);
  }
}

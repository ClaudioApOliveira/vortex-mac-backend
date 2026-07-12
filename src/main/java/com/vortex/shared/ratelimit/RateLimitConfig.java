package com.vortex.shared.ratelimit;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RateLimitConfig {

  @ConfigProperty(name = "vortex.rate-limit.enabled", defaultValue = "true")
  boolean enabled;

  @ConfigProperty(name = "vortex.rate-limit.trust-proxy", defaultValue = "false")
  boolean trustProxy;

  @ConfigProperty(name = "vortex.rate-limit.auth-requests", defaultValue = "10")
  int authRequests;

  @ConfigProperty(name = "vortex.rate-limit.auth-email-requests", defaultValue = "5")
  int authEmailRequests;

  @ConfigProperty(name = "vortex.rate-limit.auth-window-seconds", defaultValue = "60")
  int authWindowSeconds;

  @ConfigProperty(name = "vortex.rate-limit.api-requests", defaultValue = "200")
  int apiRequests;

  @ConfigProperty(name = "vortex.rate-limit.api-window-seconds", defaultValue = "60")
  int apiWindowSeconds;

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isTrustProxy() {
    return trustProxy;
  }

  public int getAuthRequests() {
    return authRequests;
  }

  public int getAuthEmailRequests() {
    return authEmailRequests;
  }

  public int getAuthWindowSeconds() {
    return authWindowSeconds;
  }

  public int getApiRequests() {
    return apiRequests;
  }

  public int getApiWindowSeconds() {
    return apiWindowSeconds;
  }
}

package com.vortex.shared.logging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RequestTimingConfig {

  @ConfigProperty(name = "vortex.request-timing.enabled", defaultValue = "true")
  boolean enabled;

  public boolean isEnabled() {
    return enabled;
  }
}

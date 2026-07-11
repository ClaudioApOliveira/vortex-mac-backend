package com.vortex.shared.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vortex.shared.ratelimit.impl.RateLimitInMemoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitInMemoryServiceImplTest {

  private RateLimitInMemoryServiceImpl rateLimitService;

  @BeforeEach
  void setUp() {
    rateLimitService = new RateLimitInMemoryServiceImpl();
  }

  @Test
  void permiteAteLimiteDaJanela() {
    String chave = "teste:ip:login";

    assertTrue(rateLimitService.tentarConsumir(chave, 3, 60));
    assertTrue(rateLimitService.tentarConsumir(chave, 3, 60));
    assertTrue(rateLimitService.tentarConsumir(chave, 3, 60));
    assertFalse(rateLimitService.tentarConsumir(chave, 3, 60));
  }
}

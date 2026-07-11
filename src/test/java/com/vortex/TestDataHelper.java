package com.vortex;

import java.util.concurrent.atomic.AtomicInteger;

public final class TestDataHelper {

  private static final AtomicInteger SEQUENCIA = new AtomicInteger();

  private TestDataHelper() {}

  public static String placaUnica() {
    int sequencia = SEQUENCIA.incrementAndGet();
    return String.format("T%04d%02d", sequencia % 10000, sequencia % 100);
  }
}

package com.vortex.shared.response;

import java.util.List;

public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {

  public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
    int safeSize = Math.max(size, 1);
    int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
    return new PageResponse<>(content, page, safeSize, totalElements, totalPages);
  }
}

package com.inventoryflow.dto.common;

import java.util.List;

/**
 * Simple page wrapper for API responses (Spring Data {@code Page} mapped to JSON).
 */
public record PageDto<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int number,
    int size
) {}

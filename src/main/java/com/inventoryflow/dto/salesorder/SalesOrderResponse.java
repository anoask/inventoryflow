package com.inventoryflow.dto.salesorder;

import java.time.Instant;
import java.util.List;

public record SalesOrderResponse(
    Long id,
    String status,
    Instant createdAt,
    List<SalesOrderItemResponse> items
) {}


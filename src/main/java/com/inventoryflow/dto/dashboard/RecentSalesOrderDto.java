package com.inventoryflow.dto.dashboard;

import java.time.Instant;

public record RecentSalesOrderDto(
    Long id,
    String status,
    Instant createdAt,
    int lineItemCount
) {}

package com.inventoryflow.dto.dashboard;

import java.time.Instant;

public record RecentPurchaseOrderDto(
    Long id,
    String status,
    Instant createdAt,
    String supplierName,
    int lineItemCount
) {}

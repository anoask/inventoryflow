package com.inventoryflow.dto.purchaseorder;

import java.time.Instant;
import java.util.List;

public record PurchaseOrderResponse(
    Long id,
    String status,
    Instant createdAt,
    Long supplierId,
    String supplierName,
    List<PurchaseOrderItemResponse> items
) {}


package com.inventoryflow.dto.purchaseorder;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
    Long productId,
    String sku,
    String name,
    int quantity,
    BigDecimal unitPrice
) {}


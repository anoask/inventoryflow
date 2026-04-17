package com.inventoryflow.dto.salesorder;

import java.math.BigDecimal;

public record SalesOrderItemResponse(
    Long productId,
    String sku,
    String name,
    int quantity,
    BigDecimal unitPrice
) {}


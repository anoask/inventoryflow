package com.inventoryflow.dto.dashboard;

import java.math.BigDecimal;

public record LowStockProductResponse(
    Long id,
    String sku,
    String name,
    String category,
    BigDecimal price,
    int quantityInStock,
    int reorderLevel,
    Long supplierId,
    String supplierName
) {}


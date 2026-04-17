package com.inventoryflow.dto.product;

import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    String description,
    String category,
    BigDecimal price,
    int quantityInStock,
    int reorderLevel,
    Long supplierId,
    String supplierName
) {}


package com.inventoryflow.dto.purchaseorder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PurchaseOrderItemCreateRequest(
    @NotNull @Positive Long productId,
    @NotNull @Min(1) int quantity,
    @DecimalMin(value = "0.0", inclusive = true) BigDecimal unitPrice
) {}


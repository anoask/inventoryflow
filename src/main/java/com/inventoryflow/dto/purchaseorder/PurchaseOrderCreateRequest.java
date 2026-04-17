package com.inventoryflow.dto.purchaseorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PurchaseOrderCreateRequest(
    @NotNull @Positive Long supplierId,
    @NotEmpty @Valid List<PurchaseOrderItemCreateRequest> items
) {}


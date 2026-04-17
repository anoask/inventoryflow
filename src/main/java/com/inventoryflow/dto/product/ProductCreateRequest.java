package com.inventoryflow.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductCreateRequest(
    @NotBlank @Size(min = 1, max = 64) String sku,
    @NotBlank @Size(max = 160) String name,
    @Size(max = 1000) String description,
    @NotBlank @Size(max = 80) String category,
    @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
    @Min(0) int quantityInStock,
    @Min(0) int reorderLevel,
    @NotNull @Positive Long supplierId
) {}


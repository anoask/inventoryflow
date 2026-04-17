package com.inventoryflow.dto.salesorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SalesOrderCreateRequest(
    @NotEmpty @Valid List<SalesOrderItemCreateRequest> items
) {}


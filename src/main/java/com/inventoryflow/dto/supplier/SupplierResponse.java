package com.inventoryflow.dto.supplier;

public record SupplierResponse(
    Long id,
    String name,
    String email,
    String phone,
    String address
) {}


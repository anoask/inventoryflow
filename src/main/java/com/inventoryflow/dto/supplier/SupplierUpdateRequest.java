package com.inventoryflow.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierUpdateRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @Email @NotBlank @Size(max = 128) String email,
    @Size(max = 32) String phone,
    @Size(max = 255) String address
) {}


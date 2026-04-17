package com.inventoryflow.dto.auth;

import java.util.List;

public record AuthResponse(
    String token,
    String tokenType,
    String email,
    String username,
    List<String> roles
) {}


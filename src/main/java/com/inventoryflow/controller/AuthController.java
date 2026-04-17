package com.inventoryflow.controller;

import com.inventoryflow.dto.auth.AuthResponse;
import com.inventoryflow.dto.auth.LoginRequest;
import com.inventoryflow.dto.auth.RegisterRequest;
import com.inventoryflow.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final AuthenticationManager authenticationManager;

  public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
    this.authService = authService;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password())
      );
      return ResponseEntity.ok(authService.issueTokenForEmail(request.email()));
    } catch (AuthenticationException ignored) {
      Map<String, Object> body = new HashMap<>();
      body.put("timestamp", Instant.now().toString());
      body.put("status", HttpStatus.UNAUTHORIZED.value());
      body.put("error", "Unauthorized");
      body.put("code", "INVALID_CREDENTIALS");
      body.put("message", "Invalid email or password");
      body.put("path", httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
  }
}


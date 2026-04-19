package com.inventoryflow.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey signingKey;
  private final long expirationMs;
  private final String issuer;

  public JwtUtil(
      @Value("${inventoryflow.jwt.secret}") String secret,
      @Value("${inventoryflow.jwt.expiration-ms}") long expirationMs,
      @Value("${inventoryflow.jwt.issuer}") String issuer
  ) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException(
          "JWT secret is missing or blank. Set INVENTORYFLOW_JWT_SECRET (prod needs SPRING_PROFILES_ACTIVE=prod)."
      );
    }
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalStateException(
          "INVENTORYFLOW_JWT_SECRET must be at least 32 UTF-8 bytes for HS256 (got "
              + keyBytes.length
              + "). Use a longer random string in Railway variables."
      );
    }
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    this.expirationMs = expirationMs;
    this.issuer = issuer;
  }

  public String generateToken(String subject, List<String> roles) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .setIssuer(issuer)
        .setSubject(subject)
        .claim("roles", roles)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}


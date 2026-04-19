package com.inventoryflow.config;

import com.inventoryflow.security.jwt.JwtAuthenticationFilter;
import com.inventoryflow.security.userdetails.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomUserDetailsService customUserDetailsService;
  private final ObjectMapper objectMapper;
  private final String corsAllowedOrigins;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter,
      CustomUserDetailsService customUserDetailsService,
      ObjectMapper objectMapper,
      @Value("${inventoryflow.cors.allowed-origins:}") String corsAllowedOrigins
  ) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.customUserDetailsService = customUserDetailsService;
    this.objectMapper = objectMapper;
    this.corsAllowedOrigins = corsAllowedOrigins;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(customUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authenticationConfiguration
  ) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.addAllowedOriginPattern("http://localhost:*");
    config.addAllowedOriginPattern("http://127.0.0.1:*");
    if (corsAllowedOrigins != null && !corsAllowedOrigins.isBlank()) {
      for (String raw : corsAllowedOrigins.split(",")) {
        String origin = raw.trim();
        if (!origin.isEmpty()) {
          config.addAllowedOriginPattern(origin);
        }
      }
    }
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    AuthenticationEntryPoint entryPoint = (request, response, authException) ->
        writeUnauthorized(request, response, authException);

    http
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .authenticationProvider(authenticationProvider())
        .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
            .requestMatchers(HttpMethod.GET, "/api/suppliers/**").hasAnyRole("ADMIN", "STAFF")
            .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

            .requestMatchers(HttpMethod.POST, "/api/suppliers/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/suppliers/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**").hasRole("ADMIN")

            .requestMatchers(HttpMethod.POST, "/api/sales-orders").hasAnyRole("ADMIN", "STAFF")
            .requestMatchers(HttpMethod.GET, "/api/sales-orders/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/sales-orders/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/sales-orders/**").hasRole("ADMIN")

            .requestMatchers("/api/purchase-orders/**").hasRole("ADMIN")

            .requestMatchers(HttpMethod.GET, "/api/dashboard/low-stock").hasAnyRole("ADMIN", "STAFF")
            .requestMatchers(HttpMethod.GET, "/api/dashboard/recent-purchase-orders").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/dashboard/recent-sales-orders").hasAnyRole("ADMIN", "STAFF")

            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  private void writeUnauthorized(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException ignored
  ) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    body.put("error", "Unauthorized");
    body.put("code", "UNAUTHENTICATED");
    body.put("message", "Missing or invalid authentication");
    body.put("path", request.getRequestURI());

    objectMapper.writeValue(response.getWriter(), body);
  }
}


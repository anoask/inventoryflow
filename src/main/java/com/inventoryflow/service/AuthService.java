package com.inventoryflow.service;

import com.inventoryflow.dto.auth.AuthResponse;
import com.inventoryflow.dto.auth.RegisterRequest;
import com.inventoryflow.exception.ConflictException;
import com.inventoryflow.model.entity.Role;
import com.inventoryflow.model.entity.User;
import com.inventoryflow.repository.RoleRepository;
import com.inventoryflow.repository.UserRepository;
import com.inventoryflow.security.jwt.JwtUtil;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new ConflictException("Email is already in use");
    }

    Role staffRole = roleRepository.findByName("STAFF")
        .orElseGet(() -> roleRepository.save(new Role("STAFF")));

    User user = new User(request.email(), request.username(), passwordEncoder.encode(request.password()));
    user.addRole(staffRole);

    userRepository.save(user);
    return issueTokenForUser(user);
  }

  public AuthResponse issueTokenForEmail(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new com.inventoryflow.exception.ResourceNotFoundException("User not found"));
    return issueTokenForUser(user);
  }

  private AuthResponse issueTokenForUser(User user) {
    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
    String token = jwtUtil.generateToken(user.getEmail(), roles);
    return new AuthResponse(token, "Bearer", user.getEmail(), user.getUsername(), roles);
  }
}


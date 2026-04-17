package com.inventoryflow.security.userdetails;

import com.inventoryflow.model.entity.User;
import com.inventoryflow.model.entity.Role;
import com.inventoryflow.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User appUser = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    List<GrantedAuthority> authorities = appUser.getRoles()
        .stream()
        .map(Role::getName)
        .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
        .collect(Collectors.toList());

    return org.springframework.security.core.userdetails.User.builder()
        .username(appUser.getEmail())
        .password(appUser.getPasswordHash())
        .authorities(authorities)
        .disabled(!appUser.isEnabled())
        .build();
  }
}


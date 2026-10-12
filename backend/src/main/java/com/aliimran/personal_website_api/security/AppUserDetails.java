package com.aliimran.personal_website_api.security;

import com.aliimran.personal_website_api.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security'nin oturumda tuttuğu kullanıcı nesnesi.
 * id'yi de taşır, böylece sahiplik kontrolünde tekrar veritabanına gitmeyiz.
 */
public class AppUserDetails implements UserDetails {

  private final Long id;
  private final String email;
  private final String displayName;
  private final String passwordHash;

  public AppUserDetails(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.displayName = user.getDisplayName();
    this.passwordHash = user.getPasswordHash();
  }

  public Long getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

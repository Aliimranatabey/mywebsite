package com.aliimran.personal_website_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Oturumdaki kullanıcıya ulaşmak için tek nokta. */
public final class CurrentUser {

  private CurrentUser() {
  }

  public static Optional<AppUserDetails> get() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()
        || !(auth.getPrincipal() instanceof AppUserDetails details)) {
      return Optional.empty();
    }
    return Optional.of(details);
  }

  public static Optional<Long> id() {
    return get().map(AppUserDetails::getId);
  }
}

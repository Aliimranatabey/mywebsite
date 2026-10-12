package com.aliimran.personal_website_api.dto;

import com.aliimran.personal_website_api.entity.User;

/** Kullanıcı bilgisinin dışarıya açılan hali — passwordHash asla burada yok. */
public record UserResponse(Long id, String email, String displayName) {

  public static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
  }
}

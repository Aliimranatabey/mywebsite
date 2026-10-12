package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.dto.RegisterRequest;
import com.aliimran.personal_website_api.entity.User;
import com.aliimran.personal_website_api.exception.BadRequestException;
import com.aliimran.personal_website_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public User register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase();

    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Bu e-posta adresi zaten kayıtlı.");
    }

    User user = new User(email, request.displayName().trim(),
        passwordEncoder.encode(request.password()));

    return userRepository.save(user);
  }
}

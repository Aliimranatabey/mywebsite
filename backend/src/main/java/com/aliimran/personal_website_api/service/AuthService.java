package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.dto.RegisterRequest;
import com.aliimran.personal_website_api.entity.User;
import com.aliimran.personal_website_api.exception.BadRequestException;
import com.aliimran.personal_website_api.exception.ForbiddenException;
import com.aliimran.personal_website_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

  /** BCrypt'in işleyebildiği en uzun şifre (bayt; Türkçe harfler 2 bayt). */
  public static final int MAX_PASSWORD_BYTES = 72;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /*
   * Kayıt açık olursa herkes hesap açıp ana sayfaya düşen yazılar yayınlayabilir.
   * Canlıda kapalı tutulur (docker-compose.yml), kendi hesabını açarken
   * .env'de REGISTRATION_ENABLED=true yapıp sonra geri alırsın.
   */
  private final boolean registrationEnabled;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      @Value("${app.registration.enabled:true}") boolean registrationEnabled) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.registrationEnabled = registrationEnabled;
  }

  public boolean isRegistrationEnabled() {
    return registrationEnabled;
  }

  @Transactional
  public User register(RegisterRequest request) {
    if (!registrationEnabled) {
      throw new ForbiddenException("Yeni üye kaydı şu anda kapalı.");
    }

    String email = request.email().trim().toLowerCase();

    if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
      throw new BadRequestException("Şifre çok uzun (Türkçe karakterlerle en fazla ~36 karakter).");
    }

    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Bu e-posta adresi zaten kayıtlı.");
    }

    User user = new User(email, request.displayName().trim(),
        passwordEncoder.encode(request.password()));

    return userRepository.save(user);
  }
}

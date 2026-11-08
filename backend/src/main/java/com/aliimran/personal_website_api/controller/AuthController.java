package com.aliimran.personal_website_api.controller;

import com.aliimran.personal_website_api.dto.LoginRequest;
import com.aliimran.personal_website_api.dto.RegisterRequest;
import com.aliimran.personal_website_api.dto.UserResponse;
import com.aliimran.personal_website_api.entity.User;
import com.aliimran.personal_website_api.security.AppUserDetails;
import com.aliimran.personal_website_api.security.CurrentUser;
import com.aliimran.personal_website_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

  public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
    this.authService = authService;
    this.authenticationManager = authenticationManager;
  }

  /** Kayıt açık mı; kapalıyken frontend "Yazmaya başla" bağlantılarını gizler. */
  @GetMapping("/registration")
  public Map<String, Boolean> registration() {
    return Map.of("enabled", authService.isRegistrationEnabled());
  }

  /** Kayıt olur ve ardından otomatik giriş yapar. */
  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request,
      HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

    User user = authService.register(request);
    authenticate(user.getEmail(), request.password(), httpRequest, httpResponse);

    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
  }

  @PostMapping("/login")
  public UserResponse login(@Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

    AppUserDetails details = authenticate(request.email().trim().toLowerCase(), request.password(),
        httpRequest, httpResponse);

    return new UserResponse(details.getId(), details.getEmail(), details.getDisplayName());
  }

  /** Oturumu sunucu tarafında iptal eder. */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    if (request.getSession(false) != null) {
      request.getSession(false).invalidate();
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.noContent().build();
  }

  /** Sayfa yenilenince frontend oturum durumunu buradan geri kurar. */
  @GetMapping("/me")
  public UserResponse me() {
    AppUserDetails details = CurrentUser.get()
        .orElseThrow(() -> new BadCredentialsException("Oturum bulunamadı"));
    return new UserResponse(details.getId(), details.getEmail(), details.getDisplayName());
  }

  /**
   * CSRF token'ını üretir ve XSRF-TOKEN çerezini yazar.
   *
   * Spring Security token'ı tembel üretir: kimse okumazsa çerez hiç yazılmaz.
   * CsrfToken'ı parametre olarak alıp getToken() çağırmak onu "dokunulmuş"
   * sayar ve çerezin yazılmasını sağlar. Frontend açılışta bunu çağırır.
   */
  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken csrfToken) {
    return Map.of("token", csrfToken.getToken(), "headerName", csrfToken.getHeaderName());
  }

  /**
   * Kimlik doğrular ve oturumu HTTP session'a yazar.
   * Session'a elle yazmak gerekiyor çünkü form login filtresini kullanmıyoruz.
   */
  private AppUserDetails authenticate(String email, String password,
      HttpServletRequest request, HttpServletResponse response) {

    // BCrypt 72 bayttan uzununu işleyemez; kontrol etmezsek 500 dönüyordu
    if (password.getBytes(StandardCharsets.UTF_8).length > AuthService.MAX_PASSWORD_BYTES) {
      throw new BadCredentialsException("Şifre çok uzun");
    }

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, password));

    // Session fixation: girişten önce elde edilmiş (ya da kurbana dayatılmış)
    // oturum kimliği girişten sonra geçersiz olsun
    if (request.getSession(false) != null) {
      request.changeSessionId();
    }

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    securityContextRepository.saveContext(context, request, response);

    return (AppUserDetails) authentication.getPrincipal();
  }
}

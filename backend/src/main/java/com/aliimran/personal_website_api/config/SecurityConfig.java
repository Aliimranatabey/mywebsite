package com.aliimran.personal_website_api.config;

import com.aliimran.personal_website_api.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import com.aliimran.personal_website_api.security.AppUserDetailsService;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CorsConfigurationSource corsConfigurationSource;
  private final RateLimitFilter rateLimitFilter;

  public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
      RateLimitFilter rateLimitFilter) {
    this.corsConfigurationSource = corsConfigurationSource;
    this.rateLimitFilter = rateLimitFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Kendi filter chain'imizi tanımladığımız için Spring Boot bunu otomatik
   * oluşturmuyor; AuthController'ın giriş yapabilmesi için elle açıyoruz.
   */
  @Bean
  public AuthenticationManager authenticationManager(AppUserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // CSRF token'ı XSRF-TOKEN çerezine yazılır (HttpOnly değil), axios onu
    // otomatik okuyup X-XSRF-TOKEN header'ı olarak geri gönderir.
    CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfRepo.setCookiePath("/");

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfRepo)
            // Spring Security 6'da token'ın istek başında çözülmesi için gerekli
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        .authorizeHttpRequests(auth -> auth
            // Okuma herkese açık
            .requestMatchers(HttpMethod.GET, "/api/blogs", "/api/blogs/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/health", "/api/sitemap.xml").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/uploads/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
            // Kayıt ve giriş
            .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/api/auth/registration").permitAll()
            // Yazma işlemleri giriş ister (sahiplik kontrolü servis katmanında)
            .requestMatchers(HttpMethod.POST, "/api/blogs").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/blogs/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/blogs/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/uploads").authenticated()
            .requestMatchers("/api/auth/**").authenticated()
            .anyRequest().permitAll())
        // SPA olduğu için giriş sayfasına yönlendirme yapma, düz 401 dön
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(HttpStatus.UNAUTHORIZED.value());
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.setCharacterEncoding("UTF-8");
              response.getWriter().write("{\"message\":\"Bu işlem için giriş yapmalısınız.\"}");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(HttpStatus.FORBIDDEN.value());
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.setCharacterEncoding("UTF-8");
              response.getWriter().write("{\"message\":\"Bu işlem için yetkiniz yok.\"}");
            }))
        .addFilterBefore(rateLimitFilter,
            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        // Varsayılan form login / basic auth kapalı, kendi uçlarımızı kullanıyoruz
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .logout(logout -> logout.disable());

    return http.build();
  }
}

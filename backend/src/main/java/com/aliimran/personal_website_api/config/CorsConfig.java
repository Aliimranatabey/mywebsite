package com.aliimran.personal_website_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

  /**
   * İzinli origin'ler ortam değişkeninden gelir (virgülle ayrılmış).
   * Canlıda nginx /api'yi proxy'lediği için frontend ve backend aynı origin
   * görünür, yani CORS devreye bile girmez. Bu ayar sadece lokal geliştirme
   * (vite dev server) ve ileride frontend'i ayrı bir yere koyarsan gerekli.
   */
  @Value("${app.cors.allowed-origins:http://localhost:5173}")
  private String allowedOrigins;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(origin -> !origin.isEmpty())
        .toList());
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("*"));
    // Oturum çerezinin ve CSRF token'ının gidip gelmesi için şart
    c.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }
}

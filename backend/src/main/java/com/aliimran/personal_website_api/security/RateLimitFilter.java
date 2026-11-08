package com.aliimran.personal_website_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kayıt, giriş ve iletişim formu için basit IP bazlı hız sınırlaması.
 * Sabit pencere yöntemi: aynı IP, aynı pencerede limitten fazla istek atarsa 429 alır.
 * Tek sunucu için yeterli; birden fazla sunucuya çıkarsan Redis'e taşınmalı.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private static final Duration WINDOW = Duration.ofMinutes(15);

  // Uç bazında pencere başına izin verilen istek sayısı
  private static final Map<String, Integer> LIMITS = Map.of(
      "POST /api/auth/register", 5,
      "POST /api/auth/login", 10,
      "POST /api/contact", 5,
      "POST /api/uploads", 30);

  private final Map<String, Counter> counters = new ConcurrentHashMap<>();

  private record Counter(Instant windowStart, AtomicInteger count) {
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String endpoint = request.getMethod() + " " + request.getRequestURI();
    Integer limit = LIMITS.get(endpoint);

    if (limit == null) {
      filterChain.doFilter(request, response);
      return;
    }

    /*
     * X-Forwarded-For'un ilk değerini okumak güvenli değildi: ziyaretçi bu
     * header'ı kendisi yazıp her istekte farklı IP gösterebiliyordu.
     * server.forward-headers-strategy=native ile Tomcat zinciri sağdan okur,
     * sadece iç ağdaki proxy'leri (nginx, Caddy) atlar; getRemoteAddr() gerçek IP'dir.
     */
    if (isOverLimit(request.getRemoteAddr() + "|" + endpoint, limit)) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(
          "{\"message\":\"Çok fazla deneme yaptınız. Lütfen bir süre sonra tekrar deneyin.\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isOverLimit(String key, int limit) {
    Instant now = Instant.now();

    Counter counter = counters.compute(key, (k, existing) -> {
      if (existing == null || Duration.between(existing.windowStart(), now).compareTo(WINDOW) > 0) {
        return new Counter(now, new AtomicInteger(0));
      }
      return existing;
    });

    return counter.count().incrementAndGet() > limit;
  }

  /**
   * Süresi geçmiş kayıtları temizler; sınırsız büyümeyi engeller.
   * Saatte bir çalışır.
   */
  @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3_600_000)
  public void evictExpired() {
    Instant now = Instant.now();
    counters.entrySet().removeIf(
        e -> Duration.between(e.getValue().windowStart(), now).compareTo(WINDOW) > 0);
  }
}

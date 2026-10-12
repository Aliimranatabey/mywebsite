package com.aliimran.personal_website_api.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Başlıktan URL'e uygun slug üretir.
 *
 * Eski hali Türkçe karakterleri siliyordu ("Merhaba Dünya" -> "merhaba-d-nya")
 * ve aynı başlıkta unique constraint'e çarpıp 500 döndürüyordu.
 */
public final class SlugGenerator {

  private static final Locale TR = Locale.of("tr", "TR");

  private SlugGenerator() {
  }

  /** Türkçe harfleri Latin karşılıklarına çevirip slug üretir. */
  public static String slugify(String input) {
    if (input == null || input.isBlank()) {
      return "yazi";
    }

    String slug = input.toLowerCase(TR)
        .replace("ı", "i").replace("İ", "i")
        .replace("ğ", "g").replace("ü", "u").replace("ş", "s")
        .replace("ö", "o").replace("ç", "c");

    // Kalan aksanlı harfleri ayrıştırıp işaretlerini at (é -> e)
    slug = Normalizer.normalize(slug, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");

    slug = slug.replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-+|-+$", "");

    if (slug.isBlank()) {
      return "yazi";
    }
    return slug.length() > 180 ? slug.substring(0, 180).replaceAll("-+$", "") : slug;
  }

  /**
   * Slug zaten kullanılıyorsa sonuna -2, -3 ... ekleyerek boş olanı bulur.
   *
   * @param isTaken verilen slug'ın kullanımda olup olmadığını söyleyen kontrol
   */
  public static String uniqueSlug(String title, Predicate<String> isTaken) {
    String base = slugify(title);

    if (!isTaken.test(base)) {
      return base;
    }

    for (int suffix = 2; suffix < 1000; suffix++) {
      String candidate = base + "-" + suffix;
      if (!isTaken.test(candidate)) {
        return candidate;
      }
    }

    // Pratikte buraya düşmez; son çare olarak zaman damgası ekle
    return base + "-" + System.currentTimeMillis();
  }
}

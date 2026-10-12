package com.aliimran.personal_website_api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SlugGeneratorTest {

  @Test
  @DisplayName("Türkçe karakterler Latin karşılıklarına çevrilir")
  void turkishCharactersAreTransliterated() {
    assertEquals("merhaba-dunya", SlugGenerator.slugify("Merhaba Dünya"));
    assertEquals("yazilim-gunlugu", SlugGenerator.slugify("Yazılım Günlüğü"));
    assertEquals("cicek-ve-sise", SlugGenerator.slugify("Çiçek ve Şişe"));
    assertEquals("istanbul", SlugGenerator.slugify("İstanbul"));
  }

  @Test
  @DisplayName("Noktalama ve fazla boşluklar tek tireye iner")
  void punctuationCollapses() {
    assertEquals("bir-iki-uc", SlugGenerator.slugify("  Bir!!  İki??  Üç  "));
  }

  @Test
  @DisplayName("Boş veya sadece sembol içeren başlıkta varsayılan slug döner")
  void blankTitleFallsBack() {
    assertEquals("yazi", SlugGenerator.slugify(""));
    assertEquals("yazi", SlugGenerator.slugify("!!!"));
    assertEquals("yazi", SlugGenerator.slugify(null));
  }

  @Test
  @DisplayName("Slug kullanımdaysa sonuna sayı eklenir (unique constraint patlamaz)")
  void duplicateSlugGetsSuffix() {
    Set<String> taken = Set.of("merhaba-dunya", "merhaba-dunya-2");

    assertEquals("merhaba-dunya-3",
        SlugGenerator.uniqueSlug("Merhaba Dünya", taken::contains));
  }

  @Test
  @DisplayName("Slug boşsa ek almaz")
  void freeSlugIsUsedAsIs() {
    assertEquals("yeni-yazi", SlugGenerator.uniqueSlug("Yeni Yazı", slug -> false));
  }
}

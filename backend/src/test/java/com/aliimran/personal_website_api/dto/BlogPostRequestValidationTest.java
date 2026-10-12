package com.aliimran.personal_website_api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlogPostRequestValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private boolean coverValid(String cover) {
    return validator.validate(new BlogPostRequest("Başlık", "<p>içerik</p>", "Test", cover)).isEmpty();
  }

  @Test
  @DisplayName("Kendi yükleme adresimiz kabul edilir")
  void acceptsOwnUploadUrl() {
    assertTrue(coverValid("/api/uploads/123e4567-e89b-12d3-a456-426614174000.jpg"));
  }

  @Test
  @DisplayName("Kapak boş bırakılabilir")
  void acceptsMissingCover() {
    assertTrue(coverValid(null));
    assertTrue(coverValid(""));
  }

  @Test
  @DisplayName("Dış adresler ve yol oyunları reddedilir")
  void rejectsForeignUrls() {
    assertFalse(coverValid("https://kotu.site/izleme.png"));
    assertFalse(coverValid("/api/uploads/../../etc/passwd"));
    assertFalse(coverValid("javascript:alert(1)"));
    assertFalse(coverValid("/api/uploads/123e4567-e89b-12d3-a456-426614174000.svg"));
  }
}

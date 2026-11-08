package com.aliimran.personal_website_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Yazı oluşturma/güncelleme gövdesi.
 * Bilinçli olarak authorId içermez — yazar her zaman oturumdan alınır,
 * yoksa kullanıcı başkasının adına yazı yayınlayabilir.
 */
public record BlogPostRequest(
    @NotBlank(message = "Başlık zorunludur") @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir") String title,

    /*
     * Sınırsız içerik, birkaç dev yazıyla diski ve yazı listesini (her kart tam
     * içeriği taşıyor) şişirmeye izin veriyordu. Base64 gömülü resimlere yer kalsın diye 2 MB.
     */
    @NotBlank(message = "İçerik zorunludur") @Size(max = 2_000_000, message = "İçerik çok uzun (en fazla ~2 MB)") String content,

    @Size(max = 60, message = "Kategori en fazla 60 karakter olabilir") String category,

    /*
     * Sadece bu sitenin yükleme ucundan dönen adresler kabul edilir.
     * Dışarıdan keyfi bir resim adresi girilmesini (izleme pikseli vb.) engeller.
     */
    @Pattern(regexp = "^(|/api/uploads/[a-f0-9-]{36}\\.(jpg|png|webp))$", message = "Geçersiz kapak görseli") String coverImageUrl) {

  /** Kapaksız istekler için kısa yol (testler ve eski çağrılar). */
  public BlogPostRequest(String title, String content, String category) {
    this(title, content, category, null);
  }
}

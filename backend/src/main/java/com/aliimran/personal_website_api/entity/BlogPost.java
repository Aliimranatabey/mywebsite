package com.aliimran.personal_website_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
public class BlogPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  private String category; // Hayat, Teknoloji, Yazılım

  @Column(nullable = false, unique = true)
  private String slug;

  /** Yüklenen kapak görselinin adresi (/api/uploads/...). Boş olabilir. */
  @Column(name = "cover_image_url")
  private String coverImageUrl;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Yazının sahibi. Sadece bu kullanıcı yazıyı güncelleyip silebilir.
   *
   * Nullable, çünkü V2__seed.sql'den gelen eski yazıların sahibi yok.
   * Sahipsiz yazılar API üzerinden düzenlenemez (403 döner).
   *
   * LAZY: yazı listesinde yazar bilgisi her zaman gerekmiyor.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  public BlogPost() {
  }

  public BlogPost(String title, String content, String category) {
    this.title = title;
    this.content = content;
    this.category = category;
  }

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (this.createdAt == null) {
      this.createdAt = now;
    }
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCoverImageUrl() {
    return coverImageUrl;
  }

  public void setCoverImageUrl(String coverImageUrl) {
    this.coverImageUrl = coverImageUrl;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }
}

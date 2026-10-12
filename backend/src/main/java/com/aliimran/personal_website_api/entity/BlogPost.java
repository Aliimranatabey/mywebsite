package com.aliimran.personal_website_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
public class BlogPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  private String category; // Hayat, Teknoloji, Yazılım

  private String slug;

  private LocalDateTime createdAt;

  public BlogPost() {
    this.createdAt = LocalDateTime.now();
  }

  public BlogPost(String title, String content, String category) {
    this.title = title;
    this.content = content;
    this.category = category;
    this.createdAt = LocalDateTime.now();
  }

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
    if (this.slug == null && this.title != null) {
      this.slug = toSlug(this.title);
    }
  }

  private String toSlug(String input) {
    String nonLatin = input.replaceAll("[^\\w-]", "-");
    return nonLatin.toLowerCase()
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-");
  }

  // Getters and Setters
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
}

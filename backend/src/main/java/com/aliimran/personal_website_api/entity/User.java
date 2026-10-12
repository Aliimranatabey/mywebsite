package com.aliimran.personal_website_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  // Şifrenin BCrypt hash'i. Asla JSON'a serileştirilmez (DTO kullanıyoruz).
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public User() {
  }

  public User(String email, String displayName, String passwordHash) {
    this.email = email;
    this.displayName = displayName;
    this.passwordHash = passwordHash;
  }

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}

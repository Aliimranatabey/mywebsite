package com.aliimran.personal_website_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "İsim zorunludur")

  @Size(max = 100, message = "İsim en fazla 100 karakter olabilir")

  private String name;
  @NotBlank(message = "E-posta zorunludur")
  @Email(message = "Geçerli bir e-posta giriniz")
  @Size(max = 200, message = "E-posta en fazla 200 karakter olabilir")
  private String email;

  @Column(columnDefinition = "TEXT")
  @NotBlank(message = "Mesaj zorunludur")
  @Size(max = 5000, message = "Mesaj en fazla 5000 karakter olabilir")
  private String message;

  private LocalDateTime createdAt;

  public ContactMessage() {
    this.createdAt = LocalDateTime.now();
  }

  public ContactMessage(String name, String email, String message) {
    this.name = name;
    this.email = email;
    this.message = message;
    this.createdAt = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}

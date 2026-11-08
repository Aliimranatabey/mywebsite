package com.aliimran.personal_website_api.controller;

import com.aliimran.personal_website_api.dto.ContactRequest;
import com.aliimran.personal_website_api.entity.ContactMessage;
import com.aliimran.personal_website_api.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

  private final ContactService contactService;

  public ContactController(ContactService contactService) {
    this.contactService = contactService;
  }

  /**
   * İletişim mesajı gönderir.
   *
   * Yanıt mailSent alanını içerir: mesaj her durumda veritabanına kaydedilir,
   * ama mail gönderilemediyse frontend bunu kullanıcıya dürüstçe söyler.
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> sendMessage(@Valid @RequestBody ContactRequest request) {
    boolean mailSent = contactService.saveMessage(
        new ContactMessage(request.name().trim(), request.email().trim(), request.message()));

    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
        "message", mailSent
            ? "Mesajınız iletildi, en kısa sürede dönüş yapacağım."
            : "Mesajınız kaydedildi. (E-posta bildirimi şu anda gönderilemedi, mesajınız kaybolmadı.)",
        "mailSent", mailSent));
  }
}

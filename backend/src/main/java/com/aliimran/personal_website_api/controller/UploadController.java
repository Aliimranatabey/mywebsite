package com.aliimran.personal_website_api.controller;

import com.aliimran.personal_website_api.service.ImageStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

  private final ImageStorageService storage;

  public UploadController(ImageStorageService storage) {
    this.storage = storage;
  }

  /** Görsel yükler (giriş gerekli). Dönen adres yazının coverImageUrl alanına yazılır. */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", storage.store(file)));
  }

  /**
   * Görseli verir (herkese açık).
   * Adlar UUID olduğu ve içerik hiç değişmediği için tarayıcı uzun süre önbellekleyebilir.
   */
  @GetMapping("/{name}")
  public ResponseEntity<Resource> get(@PathVariable String name) {
    Resource image = storage.load(name);
    return ResponseEntity.ok()
        .contentType(ImageStorageService.mediaTypeOf(name))
        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
        .body(image);
  }
}

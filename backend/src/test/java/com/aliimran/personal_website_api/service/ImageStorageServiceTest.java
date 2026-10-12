package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.exception.BadRequestException;
import com.aliimran.personal_website_api.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageStorageServiceTest {

  private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0 };
  private static final byte[] JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0 };
  private static final byte[] WEBP = { 'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P' };

  @TempDir
  Path dir;

  private ImageStorageService storage;

  @BeforeEach
  void setUp() {
    storage = new ImageStorageService(dir.toString());
  }

  @Test
  @DisplayName("PNG, JPG ve WEBP kaydedilir, adres UUID ve doğru uzantıyla döner")
  void storesSupportedTypes() throws Exception {
    String png = storage.store(new MockMultipartFile("file", "a.png", "image/png", PNG));
    String jpg = storage.store(new MockMultipartFile("file", "a.jpg", "image/jpeg", JPG));
    String webp = storage.store(new MockMultipartFile("file", "a.webp", "image/webp", WEBP));

    assertTrue(png.matches("^/api/uploads/[a-f0-9-]{36}\\.png$"));
    assertTrue(jpg.endsWith(".jpg"));
    assertTrue(webp.endsWith(".webp"));
    assertTrue(Files.exists(dir.resolve(png.substring("/api/uploads/".length()))));
  }

  @Test
  @DisplayName("Uzantısı .jpg olan ama içi HTML olan dosya reddedilir")
  void rejectsDisguisedFile() {
    byte[] html = "<script>alert(1)</script>".getBytes();
    var file = new MockMultipartFile("file", "kotu.jpg", "image/jpeg", html);

    assertThrows(BadRequestException.class, () -> storage.store(file));
  }

  @Test
  @DisplayName("5 MB'tan büyük dosya reddedilir")
  void rejectsTooLarge() {
    byte[] big = new byte[(int) ImageStorageService.MAX_BYTES + 1];
    System.arraycopy(PNG, 0, big, 0, PNG.length);

    assertThrows(BadRequestException.class,
        () -> storage.store(new MockMultipartFile("file", "b.png", "image/png", big)));
  }

  @Test
  @DisplayName("Boş dosya reddedilir")
  void rejectsEmpty() {
    assertThrows(BadRequestException.class,
        () -> storage.store(new MockMultipartFile("file", "x.png", "image/png", new byte[0])));
  }

  @Test
  @DisplayName("../ ile klasör dışına çıkma denemesi 404 olur")
  void rejectsPathTraversal() {
    assertThrows(NotFoundException.class, () -> storage.load("../application.properties"));
    assertThrows(NotFoundException.class, () -> storage.load("abc.png"));
  }

  @Test
  @DisplayName("Kaydedilen görsel geri okunabilir")
  void loadsStoredImage() throws Exception {
    String url = storage.store(new MockMultipartFile("file", "a.png", "image/png", PNG));
    String name = url.substring("/api/uploads/".length());

    assertArrayEquals(PNG, storage.load(name).getContentAsByteArray());
  }
}

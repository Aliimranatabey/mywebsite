package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.exception.BadRequestException;
import com.aliimran.personal_website_api.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Kapak görsellerini diskte saklar ve geri verir.
 *
 * Dosya türü uzantıdan değil, dosyanın ilk baytlarından (imza) anlaşılır;
 * böylece adı .jpg yapılmış bir HTML/script dosyası yüklenemez.
 * Dosya adları rastgele UUID'dir, kullanıcının verdiği ad hiç kullanılmaz.
 */
@Service
public class ImageStorageService {

  public static final long MAX_BYTES = 5L * 1024 * 1024;
  public static final String URL_PREFIX = "/api/uploads/";

  // Sadece bizim ürettiğimiz adlar okunabilir: ../ gibi yol oyunları burada elenir
  private static final Pattern STORED_NAME = Pattern.compile("^[a-f0-9-]{36}\\.(jpg|png|webp)$");

  private final Path directory;

  public ImageStorageService(@Value("${app.uploads.dir}") String directory) {
    this.directory = Path.of(directory).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.directory);
    } catch (IOException e) {
      throw new UncheckedIOException("Yükleme klasörü oluşturulamadı: " + this.directory, e);
    }
  }

  /** @return dosyanın herkese açık adresi, örn. /api/uploads/3f2a....jpg */
  public String store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("Dosya seçilmedi.");
    }
    if (file.getSize() > MAX_BYTES) {
      throw new BadRequestException("Görsel en fazla 5 MB olabilir.");
    }

    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      throw new BadRequestException("Dosya okunamadı.");
    }

    String extension = detectExtension(bytes);
    if (extension == null) {
      throw new BadRequestException("Sadece JPG, PNG veya WEBP yükleyebilirsiniz.");
    }

    String name = UUID.randomUUID() + "." + extension;
    Path target = directory.resolve(name);
    try {
      // Önce geçici dosyaya yaz, sonra taşı: yarım yazılmış dosya asla servis edilmez
      Path temp = Files.createTempFile(directory, "upload-", ".tmp");
      Files.write(temp, bytes);
      Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      throw new UncheckedIOException("Görsel kaydedilemedi", e);
    }

    return URL_PREFIX + name;
  }

  public Resource load(String name) {
    if (name == null || !STORED_NAME.matcher(name).matches()) {
      throw new NotFoundException("Görsel bulunamadı.");
    }
    Path path = directory.resolve(name);
    if (!Files.isRegularFile(path)) {
      throw new NotFoundException("Görsel bulunamadı.");
    }
    return new PathResource(path);
  }

  public static MediaType mediaTypeOf(String name) {
    if (name.endsWith(".png")) return MediaType.IMAGE_PNG;
    if (name.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
    return MediaType.IMAGE_JPEG;
  }

  /** Dosya imzasına bakarak türü bulur; tanınmıyorsa null. */
  static String detectExtension(byte[] b) {
    if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
      return "jpg";
    }
    if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
        && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
      return "png";
    }
    if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
        && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
      return "webp";
    }
    return null;
  }
}

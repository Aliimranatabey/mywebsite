package com.aliimran.personal_website_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tüm hataları tek biçimde {"message": "..."} olarak döner.
 * Böylece frontend her hatayı aynı şekilde okur ve 500 yerine doğru kod gider.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException e) {
    return body(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException e) {
    return body(HttpStatus.FORBIDDEN, e.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException e) {
    return body(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /** Hatalı e-posta/şifre. Hangisinin yanlış olduğunu söylemiyoruz (hesap keşfini engeller). */
  @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
  public ResponseEntity<Map<String, Object>> handleBadCredentials(Exception e) {
    return body(HttpStatus.UNAUTHORIZED, "E-posta veya şifre hatalı.");
  }

  /** @Valid doğrulama hataları — alan bazında mesaj döner. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors()
        .forEach(fieldError -> errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));

    String firstMessage = errors.values().stream().findFirst().orElse("Geçersiz istek.");

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("message", firstMessage);
    payload.put("errors", errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
  }

  @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, Object>> handleTooLarge(Exception e) {
    return body(HttpStatus.BAD_REQUEST, "Dosya çok büyük.");
  }

  /** Eksik "file" alanı gibi bozuk yükleme istekleri. */
  @ExceptionHandler({ org.springframework.web.multipart.support.MissingServletRequestPartException.class,
      org.springframework.web.multipart.MultipartException.class })
  public ResponseEntity<Map<String, Object>> handleBadMultipart(Exception e) {
    return body(HttpStatus.BAD_REQUEST, "Geçersiz dosya yüklemesi.");
  }

  /** Olmayan adres: genel yakalayıcıya düşüp 500 dönmesin. */
  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNoResource(Exception e) {
    return body(HttpStatus.NOT_FOUND, "Sayfa bulunamadı.");
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(Exception e) {
    return body(HttpStatus.METHOD_NOT_ALLOWED, "Bu işlem desteklenmiyor.");
  }

  /** /api/blogs/abc gibi hatalı parametre ya da bozuk JSON gövde. */
  @ExceptionHandler({ org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
      org.springframework.http.converter.HttpMessageNotReadableException.class })
  public ResponseEntity<Map<String, Object>> handleMalformed(Exception e) {
    return body(HttpStatus.BAD_REQUEST, "Geçersiz istek.");
  }

  /** Beklenmeyen hatalar: detayı loga, kullanıcıya genel mesaj. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
    log.error("Beklenmeyen hata", e);
    return body(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu.");
  }

  private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of("message", message));
  }
}

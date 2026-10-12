package com.aliimran.personal_website_api.dto;

import com.aliimran.personal_website_api.entity.BlogPost;

import java.time.LocalDateTime;

/**
 * Yazının dışarıya açılan hali. Entity'yi doğrudan dönmüyoruz,
 * yoksa author ilişkisi üzerinden passwordHash sızar.
 *
 * canEdit: isteği yapan kullanıcı bu yazının sahibi mi. Frontend butonları
 * buna göre gösterir, böylece yetki mantığı iki yerde tekrarlanmaz.
 */
public record BlogPostResponse(
    Long id,
    String title,
    String content,
    String category,
    String slug,
    String coverImageUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long authorId,
    String authorDisplayName,
    boolean canEdit) {

  public static BlogPostResponse from(BlogPost post, Long currentUserId) {
    Long authorId = post.getAuthor() != null ? post.getAuthor().getId() : null;
    String authorName = post.getAuthor() != null ? post.getAuthor().getDisplayName() : null;
    boolean canEdit = authorId != null && authorId.equals(currentUserId);

    return new BlogPostResponse(
        post.getId(), post.getTitle(), post.getContent(), post.getCategory(), post.getSlug(),
        post.getCoverImageUrl(), post.getCreatedAt(), post.getUpdatedAt(), authorId, authorName, canEdit);
  }
}

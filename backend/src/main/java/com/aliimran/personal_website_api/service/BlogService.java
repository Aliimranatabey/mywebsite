package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.dto.BlogPostRequest;
import com.aliimran.personal_website_api.entity.BlogPost;
import com.aliimran.personal_website_api.entity.User;
import com.aliimran.personal_website_api.exception.ForbiddenException;
import com.aliimran.personal_website_api.exception.NotFoundException;
import com.aliimran.personal_website_api.repository.BlogPostRepository;
import com.aliimran.personal_website_api.repository.UserRepository;
import com.aliimran.personal_website_api.util.HtmlSanitizer;
import com.aliimran.personal_website_api.util.SlugGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogService {

  private final BlogPostRepository blogPostRepository;
  private final UserRepository userRepository;

  public BlogService(BlogPostRepository blogPostRepository, UserRepository userRepository) {
    this.blogPostRepository = blogPostRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public Page<BlogPost> getAllBlogs(Pageable pageable) {
    return blogPostRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public BlogPost getBlogById(Long id) {
    return blogPostRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Blog yazısı bulunamadı: " + id));
  }

  /**
   * Yeni yazı oluşturur. Yazar her zaman oturumdaki kullanıcıdır —
   * istek gövdesinden yazar bilgisi kabul edilmez.
   */
  @Transactional
  public BlogPost createBlog(BlogPostRequest request, Long currentUserId) {
    User author = userRepository.findById(currentUserId)
        .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + currentUserId));

    BlogPost post = new BlogPost();
    post.setTitle(request.title().trim());
    post.setContent(HtmlSanitizer.clean(request.content()));
    post.setCategory(request.category());
    post.setCoverImageUrl(blankToNull(request.coverImageUrl()));
    post.setSlug(SlugGenerator.uniqueSlug(request.title(), blogPostRepository::existsBySlug));
    post.setAuthor(author);

    return blogPostRepository.save(post);
  }

  /** Sadece yazının sahibi güncelleyebilir. */
  @Transactional
  public BlogPost updateBlog(Long id, BlogPostRequest request, Long currentUserId) {
    BlogPost post = getBlogById(id);
    requireOwnership(post, currentUserId);

    // Başlık değiştiyse slug'ı da yenile, ama çakışmayı yine kontrol et
    if (!post.getTitle().equals(request.title().trim())) {
      post.setSlug(SlugGenerator.uniqueSlug(request.title(),
          candidate -> !candidate.equals(post.getSlug()) && blogPostRepository.existsBySlug(candidate)));
    }

    post.setTitle(request.title().trim());
    post.setContent(HtmlSanitizer.clean(request.content()));
    post.setCategory(request.category());
    post.setCoverImageUrl(blankToNull(request.coverImageUrl()));

    return blogPostRepository.save(post);
  }

  /** Sadece yazının sahibi silebilir. */
  @Transactional
  public void deleteBlog(Long id, Long currentUserId) {
    BlogPost post = getBlogById(id);
    requireOwnership(post, currentUserId);
    blogPostRepository.delete(post);
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  /**
   * Sahiplik kuralı. Controller'da değil burada duruyor:
   * servisi başka bir yerden çağıran kod da bu kontrolü atlayamaz.
   */
  private void requireOwnership(BlogPost post, Long currentUserId) {
    if (post.getAuthor() == null) {
      throw new ForbiddenException("Bu yazının sahibi yok, düzenlenemez.");
    }
    if (!post.getAuthor().getId().equals(currentUserId)) {
      throw new ForbiddenException("Sadece kendi yazılarınızı düzenleyip silebilirsiniz.");
    }
  }
}

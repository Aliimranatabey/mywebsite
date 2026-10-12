package com.aliimran.personal_website_api.controller;

import com.aliimran.personal_website_api.dto.BlogPostRequest;
import com.aliimran.personal_website_api.dto.BlogPostResponse;
import com.aliimran.personal_website_api.security.CurrentUser;
import com.aliimran.personal_website_api.service.BlogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

  private static final int MAX_PAGE_SIZE = 50;

  private final BlogService blogService;

  public BlogController(BlogService blogService) {
    this.blogService = blogService;
  }

  /**
   * Yazı listesi. Sayfalı döner; page/size verilmezse ilk 12 yazı gelir.
   * Okuma herkese açık, giriş gerekmez.
   */
  @GetMapping
  public Page<BlogPostResponse> getAllBlogs(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    Long currentUserId = CurrentUser.id().orElse(null);

    return blogService
        .getAllBlogs(PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt")))
        .map(post -> BlogPostResponse.from(post, currentUserId));
  }

  @GetMapping("/{id}")
  public BlogPostResponse getBlogById(@PathVariable Long id) {
    return BlogPostResponse.from(blogService.getBlogById(id), CurrentUser.id().orElse(null));
  }

  @PostMapping
  public ResponseEntity<BlogPostResponse> createBlog(@Valid @RequestBody BlogPostRequest request) {
    Long userId = requireUserId();
    BlogPostResponse created = BlogPostResponse.from(blogService.createBlog(request, userId), userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public BlogPostResponse updateBlog(@PathVariable Long id, @Valid @RequestBody BlogPostRequest request) {
    Long userId = requireUserId();
    return BlogPostResponse.from(blogService.updateBlog(id, request, userId), userId);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
    blogService.deleteBlog(id, requireUserId());
    return ResponseEntity.noContent().build();
  }

  /**
   * Bu uçlara Spring Security zaten girişsiz erişimi engelliyor;
   * bu kontrol ikinci bir emniyet kemeri.
   */
  private Long requireUserId() {
    return CurrentUser.id().orElseThrow(
        () -> new org.springframework.security.authentication.BadCredentialsException("Giriş gerekli"));
  }
}

package com.aliimran.personal_website_api.repository;

import com.aliimran.personal_website_api.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

  boolean existsBySlug(String slug);

  /** Liste ve detayda yazar bilgisini tek sorguda çeker (N+1 sorgusunu engeller). */
  @Override
  @EntityGraph(attributePaths = "author")
  Page<BlogPost> findAll(Pageable pageable);

  @Override
  @EntityGraph(attributePaths = "author")
  Optional<BlogPost> findById(Long id);
}

package com.aliimran.personal_website_api.repository;

import com.aliimran.personal_website_api.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
  // Kategoriye göre listeleme
  List<BlogPost> findByCategory(String category);
}

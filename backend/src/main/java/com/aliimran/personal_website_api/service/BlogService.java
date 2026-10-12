package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.entity.BlogPost;
import com.aliimran.personal_website_api.repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    // Tüm blogları getir
    public List<BlogPost> getAllBlogs() {
        return blogPostRepository.findAll();
    }

    // ID'ye göre blog getir
    public Optional<BlogPost> getBlogById(Long id) {
        return blogPostRepository.findById(id);
    }

    // Yeni blog kaydet
    public BlogPost saveBlog(BlogPost blogPost) {
        return blogPostRepository.save(blogPost);
    }

    // Blog sil
    public void deleteBlog(Long id) {
        blogPostRepository.deleteById(id);
    }

    // Blog güncelle
    public BlogPost updateBlog(Long id, BlogPost blogPostDetails) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog bulunamadı id: " + id));

        blogPost.setTitle(blogPostDetails.getTitle());
        blogPost.setContent(blogPostDetails.getContent());
        blogPost.setCategory(blogPostDetails.getCategory());
        // Tarihi güncellemek isterseniz:
        // blogPost.setUpdatedAt(LocalDateTime.now());

        return blogPostRepository.save(blogPost);
    }
}

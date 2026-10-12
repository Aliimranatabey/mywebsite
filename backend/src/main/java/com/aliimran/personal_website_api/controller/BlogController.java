package com.aliimran.personal_website_api.controller;

import com.aliimran.personal_website_api.entity.BlogPost;
import com.aliimran.personal_website_api.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    // Tüm blogları getir
    @GetMapping
    public List<BlogPost> getAllBlogs() {
        return blogService.getAllBlogs();
    }

    // ID'ye göre blog getir
    @GetMapping("/{id}")
    public ResponseEntity<BlogPost> getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Yeni blog ekle
    @PostMapping
    public BlogPost createBlog(@RequestBody BlogPost blogPost) {
        return blogService.saveBlog(blogPost);
    }

    // Blog sil
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok().build();
    }

    // Blog güncelle
    @PutMapping("/{id}")
    public ResponseEntity<BlogPost> updateBlog(@PathVariable Long id, @RequestBody BlogPost blogPost) {
        BlogPost updatedBlog = blogService.updateBlog(id, blogPost);
        return ResponseEntity.ok(updatedBlog);
    }
}

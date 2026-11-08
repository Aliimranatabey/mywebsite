package com.aliimran.personal_website_api.controller;

import com.aliimran.personal_website_api.entity.BlogPost;
import com.aliimran.personal_website_api.repository.BlogPostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.format.DateTimeFormatter;

/**
 * Arama motorları için site haritası (sitemap.xml).
 *
 * nginx /sitemap.xml isteğini buraya yollar. Adresler isteğin geldiği alan
 * adından kurulur (Caddy → nginx → buraya Host ve X-Forwarded-Proto ile gelir),
 * böylece alan adı koda yazılmaz.
 */
@RestController
public class SitemapController {

  private static final String[] STATIC_PAGES = {"/", "/blog", "/resume", "/contact"};

  private final BlogPostRepository blogPostRepository;

  public SitemapController(BlogPostRepository blogPostRepository) {
    this.blogPostRepository = blogPostRepository;
  }

  @GetMapping(value = "/api/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public String sitemap() {
    String base = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

    StringBuilder xml = new StringBuilder(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

    for (String page : STATIC_PAGES) {
      xml.append("  <url><loc>").append(base).append(page).append("</loc></url>\n");
    }
    for (BlogPost post : blogPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))) {
      xml.append("  <url><loc>").append(base).append("/blog/").append(post.getId())
          .append("</loc><lastmod>")
          .append(post.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE))
          .append("</lastmod></url>\n");
    }
    return xml.append("</urlset>\n").toString();
  }
}

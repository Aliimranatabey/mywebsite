package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.dto.BlogPostRequest;
import com.aliimran.personal_website_api.entity.BlogPost;
import com.aliimran.personal_website_api.entity.User;
import com.aliimran.personal_website_api.exception.ForbiddenException;
import com.aliimran.personal_website_api.exception.NotFoundException;
import com.aliimran.personal_website_api.repository.BlogPostRepository;
import com.aliimran.personal_website_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Bu testler işin kalbini koruyor: "sadece yazının sahibi düzenleyip silebilir".
 * Bu kural sessizce bozulursa herkes birbirinin yazısını silebilir hale gelir.
 */
@ExtendWith(MockitoExtension.class)
class BlogServiceOwnershipTest {

  private static final Long SAHIP_ID = 1L;
  private static final Long BASKASI_ID = 2L;

  @Mock
  private BlogPostRepository blogPostRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private BlogService blogService;

  private BlogPost yazi;
  private BlogPostRequest istek;

  @BeforeEach
  void setUp() {
    User sahip = new User("sahip@ornek.com", "Sahip", "hash");
    sahip.setId(SAHIP_ID);

    yazi = new BlogPost("Eski Başlık", "<p>içerik</p>", "Teknoloji");
    yazi.setId(10L);
    yazi.setSlug("eski-baslik");
    yazi.setAuthor(sahip);

    istek = new BlogPostRequest("Yeni Başlık", "<p>yeni içerik</p>", "Yazılım");
  }

  @Test
  @DisplayName("Sahibi kendi yazısını güncelleyebilir")
  void ownerCanUpdate() {
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));
    when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(inv -> inv.getArgument(0));

    BlogPost sonuc = blogService.updateBlog(10L, istek, SAHIP_ID);

    assertEquals("Yeni Başlık", sonuc.getTitle());
    verify(blogPostRepository).save(yazi);
  }

  @Test
  @DisplayName("Başkasının yazısını güncellemek 403 verir")
  void nonOwnerCannotUpdate() {
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));

    assertThrows(ForbiddenException.class, () -> blogService.updateBlog(10L, istek, BASKASI_ID));
    verify(blogPostRepository, never()).save(any());
  }

  @Test
  @DisplayName("Başkasının yazısını silmek 403 verir")
  void nonOwnerCannotDelete() {
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));

    assertThrows(ForbiddenException.class, () -> blogService.deleteBlog(10L, BASKASI_ID));
    verify(blogPostRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Sahibi kendi yazısını silebilir")
  void ownerCanDelete() {
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));

    blogService.deleteBlog(10L, SAHIP_ID);

    verify(blogPostRepository).delete(yazi);
  }

  @Test
  @DisplayName("Sahipsiz (eski) yazı düzenlenemez")
  void orphanPostCannotBeEdited() {
    yazi.setAuthor(null);
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));

    assertThrows(ForbiddenException.class, () -> blogService.updateBlog(10L, istek, SAHIP_ID));
    assertThrows(ForbiddenException.class, () -> blogService.deleteBlog(10L, SAHIP_ID));
  }

  @Test
  @DisplayName("Olmayan yazıda 404 döner")
  void missingPostGives404() {
    when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> blogService.updateBlog(99L, istek, SAHIP_ID));
  }

  @Test
  @DisplayName("Yeni yazının sahibi oturumdaki kullanıcı olur")
  void createAssignsCurrentUserAsAuthor() {
    User giren = new User("giren@ornek.com", "Giren", "hash");
    giren.setId(BASKASI_ID);

    when(userRepository.findById(BASKASI_ID)).thenReturn(Optional.of(giren));
    when(blogPostRepository.existsBySlug(any())).thenReturn(false);
    when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(inv -> inv.getArgument(0));

    BlogPost olusan = blogService.createBlog(istek, BASKASI_ID);

    assertEquals(BASKASI_ID, olusan.getAuthor().getId());
    assertEquals("yeni-baslik", olusan.getSlug());
  }

  @Test
  @DisplayName("Yazı kaydedilirken içerikteki script temizlenir")
  void contentIsSanitizedOnCreate() {
    User giren = new User("giren@ornek.com", "Giren", "hash");
    giren.setId(SAHIP_ID);

    when(userRepository.findById(SAHIP_ID)).thenReturn(Optional.of(giren));
    when(blogPostRepository.existsBySlug(any())).thenReturn(false);
    when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(inv -> inv.getArgument(0));

    BlogPostRequest kotuIstek = new BlogPostRequest(
        "Zararlı", "<p>selam</p><script>alert('xss')</script>", "Test");

    BlogPost olusan = blogService.createBlog(kotuIstek, SAHIP_ID);

    assertFalse(olusan.getContent().contains("script"));
    assertTrue(olusan.getContent().contains("selam"));
  }

  @Test
  @DisplayName("Sahibi kapak görselini değiştirebilir")
  void ownerCanChangeCover() {
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));
    when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(inv -> inv.getArgument(0));

    String kapak = "/api/uploads/123e4567-e89b-12d3-a456-426614174000.png";
    BlogPost sonuc = blogService.updateBlog(10L,
        new BlogPostRequest("Eski Başlık", "<p>içerik</p>", "Teknoloji", kapak), SAHIP_ID);

    assertEquals(kapak, sonuc.getCoverImageUrl());
  }

  @Test
  @DisplayName("Başkası kapak görselini değiştiremez")
  void nonOwnerCannotChangeCover() {
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));

    BlogPostRequest kapakIstegi = new BlogPostRequest("Eski Başlık", "<p>içerik</p>", "Teknoloji",
        "/api/uploads/123e4567-e89b-12d3-a456-426614174000.png");

    assertThrows(ForbiddenException.class, () -> blogService.updateBlog(10L, kapakIstegi, BASKASI_ID));
    assertNull(yazi.getCoverImageUrl());
  }

  @Test
  @DisplayName("Boş kapak null olarak kaydedilir (kapak kaldırma)")
  void blankCoverIsStoredAsNull() {
    yazi.setCoverImageUrl("/api/uploads/123e4567-e89b-12d3-a456-426614174000.png");
    when(blogPostRepository.findById(10L)).thenReturn(Optional.of(yazi));
    when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(inv -> inv.getArgument(0));

    BlogPost sonuc = blogService.updateBlog(10L,
        new BlogPostRequest("Eski Başlık", "<p>içerik</p>", "Teknoloji", ""), SAHIP_ID);

    assertNull(sonuc.getCoverImageUrl());
  }
}

package com.aliimran.personal_website_api.repository;

import com.aliimran.personal_website_api.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

  /** Verilen tarihten eski mesajları siler, silinen sayısını döner. */
  @Modifying
  @Transactional
  @Query("delete from ContactMessage m where m.createdAt < :sinir")
  int deleteOlderThan(LocalDateTime sinir);
}

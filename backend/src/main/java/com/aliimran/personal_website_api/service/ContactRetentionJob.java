package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * KVKK md. 4: kişisel veri "işlendiği amaç için gerekli süre kadar" saklanır.
 * İletişim mesajı cevaplandıktan sonra gereksiz; 1 yıldan eskiler her gece silinir.
 * Süre değişirse ContactView.vue'daki aydınlatma metnini de güncelle.
 */
@Component
public class ContactRetentionJob {

  private static final Logger log = LoggerFactory.getLogger(ContactRetentionJob.class);
  private static final int SAKLAMA_AY = 12;

  private final ContactMessageRepository repository;

  public ContactRetentionJob(ContactMessageRepository repository) {
    this.repository = repository;
  }

  @Scheduled(cron = "0 30 4 * * *")
  public void eskiMesajlariSil() {
    int silinen = repository.deleteOlderThan(LocalDateTime.now().minusMonths(SAKLAMA_AY));
    if (silinen > 0) {
      log.info("{} adet {} aydan eski iletişim mesajı silindi", silinen, SAKLAMA_AY);
    }
  }
}

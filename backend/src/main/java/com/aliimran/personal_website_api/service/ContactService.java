package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.entity.ContactMessage;
import com.aliimran.personal_website_api.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Mesajların düşeceği adres (application.properties -> app.contact.recipient)
    @Value("${app.contact.recipient}")
    private String recipient;

    // Gmail, gönderen adresin kimlik doğrulaması yapılan hesapla aynı olmasını ister
    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * Mesajı kaydeder ve mail göndermeyi dener.
     *
     * @return mail gerçekten gönderildi mi. Mesaj her durumda veritabanına yazılır,
     *         böylece mail çalışmasa bile hiçbir mesaj kaybolmaz.
     */
    public boolean saveMessage(ContactMessage message) {
        ContactMessage savedMessage = contactMessageRepository.save(message);

        // Mail gönderilemese bile mesaj veritabanında durur; ziyaretçiye hata dönmesin
        try {
            return sendEmail(savedMessage);
        } catch (Exception e) {
            log.error("İletişim maili gönderilemedi (mesaj #{} veritabanına kaydedildi): {}",
                    savedMessage.getId(), e.getMessage());
            return false;
        }
    }

    private boolean sendEmail(ContactMessage message) {
        if (!StringUtils.hasText(mailUsername)) {
            log.warn("MAIL_USERNAME tanımlı değil, mail gönderimi atlandı. "
                    + "Mesaj #{} yalnızca veritabanına kaydedildi.", message.getId());
            return false;
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailUsername);
        mailMessage.setTo(recipient);
        mailMessage.setSubject("Yeni İletişim Mesajı: " + message.getName());
        mailMessage.setText("Gönderen: " + message.getName() + " (" + message.getEmail() + ")\n\n" +
                "Mesaj:\n" + message.getMessage());

        // "Yanıtla" dendiğinde cevap doğrudan ziyaretçiye gitsin
        if (StringUtils.hasText(message.getEmail())) {
            mailMessage.setReplyTo(message.getEmail());
        }

        mailSender.send(mailMessage);
        log.info("İletişim maili gönderildi: mesaj #{} -> {}", message.getId(), recipient);
        return true;
    }
}

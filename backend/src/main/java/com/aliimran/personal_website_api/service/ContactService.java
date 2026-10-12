package com.aliimran.personal_website_api.service;

import com.aliimran.personal_website_api.entity.ContactMessage;
import com.aliimran.personal_website_api.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Mesajı veritabanına kaydet ve email gönder
    public ContactMessage saveMessage(ContactMessage message) {
        ContactMessage savedMessage = contactMessageRepository.save(message);

        // Email gönderme işlemi (Hata olursa loglayıp devam edelim, kullanıcıya hata
        // dönmesin)
        try {
            sendEmail(savedMessage);
        } catch (Exception e) {
            System.err.println("Email gönderilemedi: " + e.getMessage());
        }

        return savedMessage;
    }

    private void sendEmail(ContactMessage message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo("aliimranatabey@gmail.com");
        mailMessage.setSubject("Yeni İletişim Mesajı: " + message.getName());
        mailMessage.setText("Gönderen: " + message.getName() + " (" + message.getEmail() + ")\n\n" +
                "Mesaj:\n" + message.getMessage());

        mailSender.send(mailMessage);
    }
}

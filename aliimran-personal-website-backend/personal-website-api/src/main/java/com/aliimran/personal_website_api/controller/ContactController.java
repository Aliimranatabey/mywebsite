package com.aliimran.personal_website_api.controller;

import com.aliimran.personal_website_api.entity.ContactMessage;
import com.aliimran.personal_website_api.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    // İletişim mesajı gönder
    @PostMapping
    public ContactMessage sendMessage(@RequestBody ContactMessage message) {
        return contactService.saveMessage(message);
    }
}

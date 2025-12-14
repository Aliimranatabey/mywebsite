package com.aliimran.personal_website_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController                 // Bu sınıftan HTTP endpointleri üretilir
public class HealthController {

    @GetMapping("/api/health")  // GET /api/health isteğine cevap ver
    public String health() {
        return "ok";            // Düz metin "ok" döner
    }
}

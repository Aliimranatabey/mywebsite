package com.aliimran.personal_website_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// RateLimitFilter'ın süresi geçmiş kayıtları temizleyebilmesi için
@EnableScheduling
public class PersonalwebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalwebsiteApplication.class, args);
	}

}

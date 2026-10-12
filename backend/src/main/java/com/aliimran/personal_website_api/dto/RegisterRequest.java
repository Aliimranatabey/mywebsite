package com.aliimran.personal_website_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "E-posta zorunludur") @Email(message = "Geçerli bir e-posta giriniz") String email,

    @NotBlank(message = "Görünen ad zorunludur") @Size(min = 2, max = 60, message = "Görünen ad 2-60 karakter olmalıdır") String displayName,

    @NotBlank(message = "Şifre zorunludur") @Size(min = 8, max = 100, message = "Şifre en az 8 karakter olmalıdır") String password) {
}

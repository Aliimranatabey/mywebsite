package com.aliimran.personal_website_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * İletişim formu gövdesi.
 * Entity'yi doğrudan almıyoruz: gövdeye "id" yazan ziyaretçi, kayıtlı başka
 * bir mesajın üzerine yazabiliyordu (createdAt'i de sahteleyebiliyordu).
 */
public record ContactRequest(
    @NotBlank(message = "İsim zorunludur") @Size(max = 100, message = "İsim en fazla 100 karakter olabilir") String name,

    @NotBlank(message = "E-posta zorunludur") @Email(message = "Geçerli bir e-posta giriniz") @Size(max = 200, message = "E-posta en fazla 200 karakter olabilir") String email,

    @NotBlank(message = "Mesaj zorunludur") @Size(max = 5000, message = "Mesaj en fazla 5000 karakter olabilir") String message) {
}

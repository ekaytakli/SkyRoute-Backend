package com.flightapp.flightticketsystem.jwt;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Kullanıcı giriş (Login) isteklerinde istemciden (Frontend) gelen
 * e-posta ve şifre verilerini taşıyan ve doğrulayan istek modelidir (DTO).
 */
@Data // Lombok: Sınıftaki alanlar için Getter, Setter, toString, equals ve hashCode metotlarını otomatik üretir.
public class LoginRequest {

    /**
     * Kullanıcının sisteme kayıtlı olduğu e-posta adresi.
     * Boş bırakılamaz ve geçerli bir e-posta formatında olmalıdır.
     */
    @NotBlank(message = "{email.cannotblank}") // Alanın null, boş string veya sadece boşluklardan oluşmasını engeller.
    @Email(message = "{email.validation}") // Girilen metnin geçerli bir e-posta formatında (örn: kullanici@alanadi.com) olmasını denetler.
    private String email;

    /**
     * Kullanıcının sisteme giriş yaparken girdiği açık metin şifresi.
     * Boş bırakılamaz.
     */
    @NotBlank(message = "{password.validation}") // Şifre alanının boş bırakılmasını engeller.
    private String password;
}
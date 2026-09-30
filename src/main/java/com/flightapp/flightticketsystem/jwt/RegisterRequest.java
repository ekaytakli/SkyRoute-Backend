package com.flightapp.flightticketsystem.jwt;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Kullanıcı kayıt (Register) isteklerinde istemciden (Frontend) gelen
 * ad, soyad, e-posta ve şifre verilerini taşıyan ve doğrulayan istek modelidir (DTO).
 */
@Data // Lombok: Alanlar için Getter, Setter, toString, equals ve hashCode metotlarını otomatik üretir.
public class RegisterRequest {

    /**
     * Kullanıcının sisteme kaydolacağı e-posta adresi.
     * Boş bırakılamaz ve geçerli bir e-posta formatına sahip olmalıdır.
     */
    @NotBlank(message = "{email.cannotblank}") // Alanın null, boş string veya yalnızca boşluklardan oluşmasını engeller.
    @Email(message = "{email.validation}") // Formatın geçerli bir e-posta yapısına (örn: ornek@mail.com) uymasını zorunlu tutar.
    private String email;

    /**
     * Kullanıcının hesap güvenliği için belirlediği şifre.
     * Boş bırakılamaz.
     */
    @NotBlank(message = "{password.validation}") // Şifre alanının boş bırakılmasını engeller.
    private String password;

    /**
     * Kullanıcının adı.
     * Bilet üzerinde ve rezervasyon kayıtlarında kullanılmak üzere zorunludur.
     */
    @NotBlank(message = "{firstName.validation}") // Ad alanının boş bırakılmasını engeller.
    private String firstName;

    /**
     * Kullanıcının soyadı.
     * Bilet üzerinde ve fatura/yolcu kayıtlarında kullanılmak üzere zorunludur.
     */
    @NotBlank(message = "{lastName.validation}") // Soyad alanının boş bırakılmasını engeller.
    private String lastName;
}
package com.flightapp.flightticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Kullanıcı ödeme yapıp bilet satın almak istediğinde formdan gelen tüm verileri taşır.
 * Hem rezervasyon yapılacak seferi hem de ödeme için gereken kredi kartı bilgilerini barındırır.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    // Satın alınmak istenen uçuşun benzersiz kimlik numarası
    @NotNull(message = "{error.flight_id_null}")
    @Positive(message = "{error.id_positive}")
    private Integer flightId;

    // Uçuş içinde seçilen koltuğun benzersiz kimlik numarası
    @NotNull(message = "{error.seat_id_null}")
    @Positive(message = "{error.id_positive}")
    private Integer seatId;

    // Uyumluluk icin korunur; odeme tutari sunucuda koltuk fiyatindan alinir.
    private BigDecimal amount;

    // Yolcunun adı
    @NotBlank(message = "{error.first_name_required}")
    private String firstName;

    // Yolcunun soyadı
    @NotBlank(message = "{error.last_name_required}")
    private String lastName;

    // Bilet ve fatura bilgilerinin gönderileceği e-posta
    @NotBlank(message = "{error.email_required}")
    @Email(message = "{error.email_invalid}")
    private String email;

    // İletişim telefon numarası
    private String phone;

    // Kredi kartı üzerindeki isim ve soyisim
    @NotBlank(message = "{error.card_holder_required}")
    private String cardHolderName;

    // Kredi kartı numarası (16 haneli)
    @NotBlank(message = "{error.card_number_invalid}")
    @Pattern(regexp = "[0-9]{13,19}", message = "{error.card_number_invalid}")
    private String cardNumber;

    // Kartın son kullanma ayı (örn: "12")
    @NotBlank(message = "{error.expire_month_invalid}")
    @Pattern(regexp = "0[1-9]|1[0-2]", message = "{error.expire_month_invalid}")
    private String expireMonth;

    // Kartın son kullanma yılı (örn: "2028")
    @NotBlank(message = "{error.expire_year_invalid}")
    @Pattern(regexp = "[0-9]{4}", message = "{error.expire_year_invalid}")
    private String expireYear;

    // Kartın güvenlik kodu (arka yüzdeki 3 haneli kod)
    @NotBlank(message = "{error.cvc_invalid}")
    @Pattern(regexp = "[0-9]{3,4}", message = "{error.cvc_invalid}")
    private String cvc;
}

package com.flightapp.flightticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    private Integer flightId;

    // Uçuş içinde seçilen koltuğun benzersiz kimlik numarası
    private Integer seatId;

    // İstemciden iletilen tutar (Arka planda güvenlik amacıyla veritabanındaki fiyatla karşılaştırılır)
    private BigDecimal amount;

    // Yolcunun adı
    private String firstName;

    // Yolcunun soyadı
    private String lastName;

    // Bilet ve fatura bilgilerinin gönderileceği e-posta
    private String email;

    // İletişim telefon numarası
    private String phone;

    // Kredi kartı üzerindeki isim ve soyisim
    private String cardHolderName;

    // Kredi kartı numarası (16 haneli)
    private String cardNumber;

    // Kartın son kullanma ayı (örn: "12")
    private String expireMonth;

    // Kartın son kullanma yılı (örn: "2028")
    private String expireYear;

    // Kartın güvenlik kodu (arka yüzdeki 3 haneli kod)
    private String cvc;
}
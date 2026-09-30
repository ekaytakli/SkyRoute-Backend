package com.flightapp.flightticketsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Uygulama genelinde bir hata fırlatıldığında istemciye (Frontend) döndürülecek
 * standart JSON hata yanıt modelini temsil eden DTO sınıfıdır.
 */
@Data // Lombok: Getter, Setter, toString, equals ve hashCode metotlarını otomatik üretir.
@NoArgsConstructor // Lombok: Parametresiz varsayılan yapıcı metot oluşturur.
@AllArgsConstructor // Lombok: Tüm alanları içeren yapıcı metot oluşturur.
public class ErrorResponse {

    // HTTP durum kodu değerini tutar (Örn: 400, 404, 500)
    private int status;

    // Kullanıcıya gösterilecek olan anlaşılır hata mesajı metni
    private String message;

    // Hatanın sunucu tarafında oluştuğu anın tarih ve saat bilgisi
    private LocalDateTime timestamp;

    /**
     * Sadece durum kodu ve mesaj alarak zaman damgasını (timestamp)
     * otomatik olarak o anki sistem saatiyle dolduran pratik kurucu metot (Constructor).
     *
     * @param status HTTP durum kodu (Örn: 404).
     * @param message Çözümlenmiş hata açıklaması.
     */
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        // Hatanın oluştuğu anı otomatik olarak sistem saatinden alır.
        this.timestamp = LocalDateTime.now();
    }
}
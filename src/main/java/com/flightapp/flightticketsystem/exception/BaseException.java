package com.flightapp.flightticketsystem.exception;

import lombok.Getter;

/**
 * Uygulama genelindeki iş kuralları (business logic) ihlallerinde fırlatılan
 * temel ve özelleştirilmiş çalışma zamanı istisnasıdır (Custom RuntimeException)[cite: 1].
 *
 * Doğrudan sabit bir metin yerine bir 'messageKey' taşıyarak
 * çoklu dil (i18n) destekli hata mesajı üretilmesine imkan tanır[cite: 1].
 */
@Getter // Lombok: messageKey ve args alanları için getter metotlarını otomatik üretir[cite: 1].
public class BaseException extends RuntimeException {

    // messages.properties dosyalarındaki çeviri anahtarını tutar (Örn: "error.flight_not_found")[cite: 1]
    private final String messageKey;

    // Hata mesajı metninin içine dinamik olarak yerleştirilecek ek parametreler dizisi (Örn: uçuş no, ID vb.)[cite: 1]
    private final Object[] args;

    /**
     * Parametresiz, yalnızca hata anahtarı alan kurucu metot (Constructor)[cite: 1].
     * Dinamik parametre gerektirmeyen standart hata durumlarında kullanılır[cite: 1].
     *
     * @param messageKey messages.properties dosyasında karşılığı bulunan hata kodu/anahtarı[cite: 1].
     */
    public BaseException(String messageKey) {
        // Üst sınıf olan RuntimeException'a anahtar kelimeyi varsayılan mesaj olarak iletir[cite: 1].
        super(messageKey);
        this.messageKey = messageKey;
        // Parametre verilmediği için boş bir nesne dizisi oluşturur[cite: 1].
        this.args = new Object[]{};
    }

    /**
     * Hata anahtarı ile birlikte dinamik parametreleri de kabul eden kurucu metot[cite: 1].
     * Örneğin: "error.seat_reserved_by", "Ali" gibi durumlarda mesajın içine değişken basılmasını sağlar[cite: 1].
     *
     * @param messageKey Hata anahtarı[cite: 1].
     * @param args Mesaj şablonu içindeki {0}, {1} gibi yer tutuculara yerleşecek dinamik değişkenler[cite: 1].
     */
    public BaseException(String messageKey, Object... args) {
        // Üst sınıfa anahtar kelimeyi mesaj olarak iletir[cite: 1].
        super(messageKey);
        this.messageKey = messageKey;
        // Gelen değişken parametreleri diziye atar[cite: 1].
        this.args = args;
    }
}
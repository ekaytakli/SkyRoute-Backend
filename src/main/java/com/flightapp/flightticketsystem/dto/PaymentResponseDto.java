package com.flightapp.flightticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ödeme işlem denemesinin ardından istemciye iletilen sonuç modeli.
 * İşlemin başarılı olup olmadığını ve başarılıysa kesilen biletin PNR kodunu iletir.
 */
@Getter
@Setter
@Builder // Lombok: Builder pattern ile nesne üretimini kolaylaştırır.
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    // Ödeme akışının nihai durumu ("SUCCESS" veya "FAILED")
    private String status;

    // Sanal POS (iyzico) tarafından tahsis edilen işlem referans/ödeme takip numarası
    private String paymentId;

    // Ödeme başarılıysa oluşturulan biletin 6 haneli benzersiz PNR rezervasyon kodu
    private String pnrKodu;

    // Kullanıcıya arayüzde gösterilecek bilgilendirme veya hata mesajı
    private String message;
}
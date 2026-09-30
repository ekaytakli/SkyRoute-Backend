package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.dto.PaymentRequestDto;
import com.flightapp.flightticketsystem.dto.PaymentResponseDto;
import com.flightapp.flightticketsystem.service.IPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * İstemciden (frontend) gelen ödeme ve bilet satın alma isteklerini karşılayan REST Controller bileşenidir[cite: 1].
 *
 * Bu uç nokta SecurityConfig konfigürasyonunda 'permitAll' olarak tanımlandığı için
 * kullanıcılar sisteme üye olmadan da (public flow) bu adresi çağırabilir[cite: 1].
 */
@RestController // Bu sınıfın REST tabanlı bir denetleyici olduğunu ve dönen sonuçların doğrudan HTTP gövdesine JSON olarak yazılacağını bildirir[cite: 1].
@RequestMapping("/api/v1/payments") // Sınıf içindeki tüm ödeme uç noktalarının temel URL yolunu (/api/v1/payments) belirler[cite: 1].
@RequiredArgsConstructor // 'final' anahtar sözcüğü ile işaretlenmiş olan IPaymentService bağımlılığı için Constructor Injection sağlar[cite: 1].
public class PaymentController {

    // Ödeme doğrulama, iyzico entegrasyonu ve bilet oluşturma iş mantığını yürüten servis arayüzü[cite: 1].
    private final IPaymentService paymentService;

    /**
     * Yeni bir ödeme ve biletleme sürecini başlatan HTTP POST uç noktasıdır[cite: 1].
     * Rota: POST /api/v1/payments[cite: 1]
     *
     * @param requestDto Kullanıcının girdiği kart bilgileri, iletişim detayları,
     *                   satın alınmak istenen uçuş ve koltuk ID'lerini içeren veri transfer nesnesi[cite: 1].
     *                   @Valid: Gelen DTO alanlarının doğrulama kurallarına uygunluğunu kontrol eder[cite: 1].
     *                   @RequestBody: HTTP istek gövdesindeki JSON verisini Java PaymentRequestDto nesnesine dönüştürür[cite: 1].
     * @return Başarı durumunda oluşturulan PNR kodunu, banka işlem ID'sini ve durum mesajını içeren PaymentResponseDto[cite: 1].
     */
    @PostMapping
    public PaymentResponseDto createPayment(
            @Valid @RequestBody PaymentRequestDto requestDto
    ) {
        // Gelen ödeme parametrelerini iş mantığını çalıştırması için PaymentServiceImpl sınıfına devreder[cite: 1].
        // İşlem tamamlandığında üretilen sonucu istemciye döner[cite: 1].
        return paymentService.createPayment(requestDto);
    }
}
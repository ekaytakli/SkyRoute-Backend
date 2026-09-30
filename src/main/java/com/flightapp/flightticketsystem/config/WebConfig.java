package com.flightapp.flightticketsystem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC web katmanının ayarlarını özelleştiren konfigürasyon sınıfıdır.
 * WebMvcConfigurer arayüzü sayesinde Spring MVC'nin varsayılan davranışlarına müdahale edilebilir.
 */
@Configuration // Spring Boot'a bu sınıfın bir web yapılandırma bileşeni olduğunu bildirir.
@RequiredArgsConstructor // 'final' olarak tanımlanan RateLimitInterceptor bağımlılığı için yapıcı metot (Constructor) üretir.
public class WebConfig implements WebMvcConfigurer {

    // IP bazlı istek sınırlandırma kontrolünü yapan önleyici interceptor bileşeni
    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * Uygulamadaki araya girici filtreleri (interceptor) ve bu filtrelerin
     * hangi endpoint rotalarında çalışacağını kayıt altına alan metottur.
     *
     * @param registry Interceptor'ların kaydedildiği ve rota desenlerinin eşleştirildiği kayıt defteri.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. Adım: Hazırladığımız hız sınırlama filtresini (RateLimitInterceptor) Spring MVC kayıt zincirine ekle.
        registry.addInterceptor(rateLimitInterceptor)
                // 2. Adım: Bu hız sınırını sadece login endpoint'ine uygula.
                // Bu sayede brute-force (kaba kuvvet şifre deneme) saldırıları engellenirken,
                // uçuş arama gibi genel sayfalarda kullanıcılar gereksiz yere limitlenmez.
                .addPathPatterns("/api/v1/auth/login");
    }
}
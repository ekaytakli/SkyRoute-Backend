package com.flightapp.flightticketsystem.config;

import com.iyzipay.Options;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * iyzico ödeme sisteminin entegrasyon ayarlarını yöneten konfigürasyon sınıfıdır.
 *
 * Hassas API anahtarlarını kaynak dosyalarından (application.properties) okur
 * ve ödeme isteklerinde kullanılacak Options nesnesini Spring IoC konteynerine tanıtır.
 */
@Configuration // Spring Boot'a bu sınıfın Bean tanımları içeren bir yapılandırma sınıfı olduğunu belirtir.
public class IyzicoConfig {

    // application.properties dosyasındaki 'iyzico.api-key' değerini otomatik olarak bu değişkene enjekte eder.
    // Bu anahtar iyzico üzerindeki mağazanızı/hesabınızı tanımlayan açık kimliktir.
    @Value("${iyzico.api-key}")
    private String apiKey;

    // application.properties dosyasındaki 'iyzico.secret-key' değerini okur.
    // İstekleri imzalamak ve güvenliği sağlamak için kullanılan gizli şifredir.
    @Value("${iyzico.secret-key}")
    private String secretKey;

    // application.properties dosyasındaki 'iyzico.base-url' değerini okur.
    // Test aşamasında Sandbox ortamı (örn: https://sandbox-api.iyzipay.com), canlıda gerçek iyzico API adresidir.
    @Value("${iyzico.base-url}")
    private String baseUrl;

    /**
     * iyzico resmi Java kütüphanesinin (SDK) tüm API isteklerinde talep ettiği
     * bağlantı parametrelerini (Options) hazırlayıp bir Spring Bean'i olarak sunar.
     *
     * Bu sayede PaymentServiceImpl gibi sınıflar 'Options iyzicoOptions' nesnesini
     * doğrudan enjekte ederek kullanabilir.
     *
     * @return Yapılandırılmış ve kimlik bilgileri yüklenmiş iyzico Options nesnesi.
     */
    @Bean // Bu metodun ürettiği Options nesnesini Spring'in uygulama hafızasında tekil (Singleton) olarak yönetmesini sağlar.
    public Options iyzicoOptions() {
        // iyzico kütüphanesine ait ayar nesnesi oluşturulur.
        Options options = new Options();

        // Okunan API anahtarı nesneye atanır.
        options.setApiKey(apiKey);

        // Okunan gizli anahtar (Secret Key) nesneye atanır.
        options.setSecretKey(secretKey);

        // İsteklerin yönlendirileceği API uç noktası adresi ayarlanır.
        options.setBaseUrl(baseUrl);

        // Hazırlanan ayar paketi dışarıya sunulur.
        return options;
    }
}
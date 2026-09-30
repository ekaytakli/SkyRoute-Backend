package com.flightapp.flightticketsystem.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gelen HTTP isteklerini Controller katmanına ulaşmadan önce yakalayan
 * ve IP adresi bazlı hız sınırlandırması (Rate Limiting) uygulayan önleyici filtredir.
 */
@Component // Spring konteyneri tarafından yönetilen genel bir bileşen olduğunu belirtir.
public class RateLimitInterceptor implements HandlerInterceptor {

    // Her bir istemcinin (IP adresinin) kendine ait jeton kovasını (Bucket)
    // thread-safe (eş zamanlı erişime uygun) olarak bellekte tutan önbellek haritası.
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * İstek yapan IP adresine ait mevcut bir kova olup olmadığını kontrol eder.
     * Varsa o kovayı döner, yoksa yeni bir kova üretip önbelleğe ekler.
     *
     * @param key İstemcinin IP adresi.
     * @return İstemciye tahsis edilmiş Bucket nesnesi.
     */
    private Bucket resolveBucket(String key) {
        // İlgili anahtar (IP) haritada yoksa newBucket metodunu çağırarak yenisini oluşturur ve kaydeder.
        return cache.computeIfAbsent(key, this::newBucket);
    }

    /**
     * Yeni bir IP adresi için hız sınırı kurallarını (Bandwidth) barındıran kovayı yapılandırır.
     *
     * @param key İstemci IP adresi.
     * @return Hız limiti tanımlanmış Bucket nesnesi.
     */
    private Bucket newBucket(String key) {
        // Kural: Kovanın maksimum kapasitesi 5 jetondur.
        // Refill.greedy: 1 dakikalık süre içinde toplam 5 jeton kademeli olarak kovaya yeniden eklenir.
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));

        // Tanımlanan bant genişliği/limit kuralı ile kovayı inşa eder.
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * İstek Controller metoduna ulaşmadan hemen ÖNCE çalıştırılan kontrol metodudur.
     *
     * @param request Gelen HTTP istek nesnesi (IP adresi buradan okunur).
     * @param response İstemciye dönülecek HTTP yanıt nesnesi.
     * @param handler İsteği karşılayacak olan hedef controller/metot.
     * @return true ise istek Controller'a geçebilir; false ise istek burada kesilir ve durdurulur.
     * @throws Exception Yazma veya yönlendirme sırasında oluşabilecek hatalar için.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. Adım: İsteği gönderen istemcinin IP adresini al.
        String clientIp = request.getRemoteAddr();

        // 2. Adım: Bu IP adresine atanmış olan jeton kovasını getir.
        Bucket bucket = resolveBucket(clientIp);

        // 3. Adım: Kovadan 1 adet jeton harcamayı dene.
        if (bucket.tryConsume(1)) {
            // Kovada yeterli jeton varsa isteğin Controller'a geçmesine izin ver.
            return true;
        } else {
            // 4. Adım: Jeton kalmadıysa (hız sınırı aşıldıysa) HTTP 429 Too Many Requests durum kodunu ata.
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            // Yanıt gövdesine açıklayıcı hata mesajını yaz.
            response.getWriter().write("Too many requests. Please try again later.");

            // İsteğin Controller'a gitmesini engelle ve işlemi burada sonlandır.
            return false;
        }
    }
}
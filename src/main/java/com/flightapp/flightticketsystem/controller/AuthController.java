package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.jwt.LoginRequest;
import com.flightapp.flightticketsystem.jwt.RegisterRequest;
import com.flightapp.flightticketsystem.jwt.AuthResponse;
import com.flightapp.flightticketsystem.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kullanıcı kimlik doğrulama (Kayıt olma ve Giriş yapma) isteklerini karşılayan
 * REST Controller sınıfıdır[cite: 1].
 */
@RestController // Bu sınıfın REST API uç noktalarını yönettiğini ve metotların doğrudan JSON/XML yanıt döndüğünü belirtir[cite: 1].
@RequestMapping("/api/v1/auth") // Bu denetleyicideki tüm uç noktaların temel URL ön ekini (/api/v1/auth) belirler[cite: 1].
@RequiredArgsConstructor // 'final' anahtar kelimesiyle tanımlanmış IAuthService bağımlılığı için Constructor Injection sağlar[cite: 1].
public class AuthController {

    // Kimlik doğrulama iş mantığını yürüten servis arayüzü[cite: 1]
    private final IAuthService authService;

    /**
     * Yeni bir kullanıcının sisteme kayıt olmasını sağlayan HTTP POST uç noktası[cite: 1].
     * Rota: POST /api/v1/auth/register[cite: 1]
     *
     * @param request İstemciden JSON formatında gelen ad, soyad, e-posta ve şifre verilerini içeren nesne[cite: 1].
     *                @Valid: DTO üzerindeki anotasyonlara (örn: @NotBlank) göre verilerin doğruluğunu kontrol eder[cite: 1].
     *                @RequestBody: HTTP istek gövdesindeki (body) ham JSON verisini Java nesnesine dönüştürür[cite: 1].
     * @return 200 OK durum kodu ile birlikte üretilen JWT token'ı içeren AuthResponse nesnesi[cite: 1].
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // İşi doğrudan servis katmanına devreder ve sonucu ResponseEntity ile sarmalayarak döner[cite: 1].
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Mevcut bir kullanıcının e-posta ve şifresiyle sisteme giriş yapmasını sağlayan HTTP POST uç noktası[cite: 1].
     * Rota: POST /api/v1/auth/login[cite: 1]
     *
     * @param request İstemciden gelen e-posta ve şifre bilgilerini taşıyan DTO[cite: 1].
     * @return 200 OK durum kodu ile birlikte kullanıcı için üretilen geçerli JWT token'ı içeren AuthResponse nesnesi[cite: 1].
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Giriş kontrolünü yapması için isteği authService.login() metoduna iletir ve başarılı ise token'ı döner[cite: 1].
        return ResponseEntity.ok(authService.login(request));
    }
}
package com.flightapp.flightticketsystem.config;

import com.flightapp.flightticketsystem.jwt.AuthEntryPoint;
import com.flightapp.flightticketsystem.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security'nin tüm ağ/erişim güvenliğini, HTTP kurallarını,
 * JWT filtresini ve oturum yönetimini yapılandıran merkezi güvenlik sınıfıdır.
 */
@Configuration // Spring Boot'a bir konfigürasyon sınıfı olduğunu bildirir.
@EnableWebSecurity // Spring Security'nin web güvenliği desteğini aktif hale getirir.
@EnableMethodSecurity // Controller metotlarında @PreAuthorize gibi rol/yetki anotasyonlarının çalışmasını sağlar.
@RequiredArgsConstructor // 'final' tanımlı bağımlılıkları Constructor Injection ile otomatik bağlar.
public class SecurityConfig {

    // Gelen her HTTP isteğindeki Authorization başlığından (Bearer token) JWT'yi çıkaran filtre.
    private final JwtAuthenticationFilter jwtAuthFilter;

    // Kullanıcı doğrulama ve şifre karşılaştırma adımlarını yöneten sağlayıcı (AppConfig içinde tanımlı).
    private final AuthenticationProvider authenticationProvider;

    // Yetkisiz veya geçersiz token ile gelen isteklerde özel JSON hata mesajı dönen giriş noktası.
    private final AuthEntryPoint authEntryPoint;

    /**
     * HTTP isteklerinin geçeceği güvenlik filtre zincirini (SecurityFilterChain) inşa eder.
     *
     * @param http Güvenlik kurallarını yapılandırmak için kullanılan HttpSecurity nesnesi.
     * @return Yapılandırılmış SecurityFilterChain nesnesi.
     * @throws Exception Güvenlik zinciri oluşturulurken çıkabilecek hatalar için.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // REST API mimarisinde sunucuda cookie/session tutulmadığı ve JWT kullanıldığı için CSRF (Cross-Site Request Forgery) koruması kapatılır.
                .csrf(csrf -> csrf.disable())

                // Kimliği doğrulanmamış (401 Unauthorized) istekler yakalandığında devreye girecek özel hata yakalayıcıyı atar.
                .exceptionHandling(
                        exception ->
                                exception.authenticationEntryPoint(
                                        authEntryPoint
                                )
                )

                // URL tabanlı erişim izinlerini ve yetkilendirme kurallarını belirler.
                .authorizeHttpRequests(auth -> auth

                        // 1. Kural: Kayıt olma ve giriş yapma endpoint'leri herkese açıktır.
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        // 2. Kural: Ziyaretçiler üye olmadan da uçuşları arayıp listeleyebilsin diye GET istekleri herkese açıktır.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/rest/api/flight/**"
                        ).permitAll()

                        // 3. Kural: Ziyaretçiler bir uçuş seçtiğinde koltuk durumlarını görebilsin diye koltuk GET istekleri serbesttir.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/seats/**"
                        ).permitAll()

                        // Doğrudan ödemesiz bilet oluşturma yalnızca yöneticiye açıktır.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/tickets"
                        ).hasRole("ADMIN")

                        // 5. Kural: Ziyaretçiler ödeme adımını tamamlayabilsin diye ödeme başlatma POST isteği herkese açıktır.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/payments/**"
                        ).permitAll()

                        // 6. Kural: API test ve dokümantasyon araçları (Swagger UI / OpenAPI) giriş yapmadan görüntülenebilir.
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 7. Kural: Yukarıda açıkça 'permitAll' verilmeyen tüm diğer endpoint'ler için JWT kimlik doğrulaması zorunludur.
                        .anyRequest().authenticated()
                )

                // Oturum yönetimini STATELESS (durumsuz) yapar; sunucu oturum (HttpSession) saklamaz, her istek kendi token'ını taşır.
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                // Kimlik doğrulamada AppConfig'de tanımladığımız DaoAuthenticationProvider mekanizmasını kullanır.
                .authenticationProvider(
                        authenticationProvider
                )

                // JWT filtresini, Spring Security'nin standart kullanıcı adı/şifre kontrol filtresinin ÖNÜNE yerleştirir.
                // Böylece istek henüz standart login zincirine girmeden JWT doğrulanmış olur.
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        // Hazırlanan güvenlik zincirini derleyip döner.
        return http.build();
    }
}

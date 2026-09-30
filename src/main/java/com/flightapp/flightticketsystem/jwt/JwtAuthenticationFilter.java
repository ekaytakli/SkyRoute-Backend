package com.flightapp.flightticketsystem.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Gelen her HTTP isteğini Controller katmanından önce yakalayan ve
 * istek başlığındaki JWT token'ı çözümleyip doğrulayan güvenlik filtresidir.
 *
 * OncePerRequestFilter sınıfından türediği için her istek döngüsünde
 * garanti olarak yalnızca bir kez çalıştırılır.
 */
@Component // Spring'in bu sınıfı bir filtre bileşeni olarak yönetmesini sağlar.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Veritabanından kullanıcı bilgilerini ve rollerini yükleyen servis
    private final UserDetailsService userDetailsService;

    // JWT token ayrıştırma, kullanıcı adı çekme ve süre kontrolü yapan servis
    private final JwtService jwtService;

    // Bağımlılıkların dışarıdan enjekte edilmesini sağlayan kurucu metot (Constructor Injection)
    public JwtAuthenticationFilter(UserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    /**
     * Gelen isteğin içindeki JWT bilgisini okuyup kullanıcıyı Spring Security'ye tanıtan ana metottur.
     *
     * @param request     Gelen HTTP istek nesnesi
     * @param response    Dönecek olan HTTP yanıt nesnesi
     * @param filterChain Sıradaki diğer filtreleri tetikleyen zincir yapısı
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Adım: HTTP başlığından 'Authorization' bilgisini oku (Örn: "Bearer eyJhbGciOi...")
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String userName;

        // 2. Adım: Başlık boşsa veya "Bearer " ile başlamıyorsa işlem yapma, zincirdeki sıradaki filtreye geç
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Adım: "Bearer " (7 karakter) kısmını keserek saf token dizesini elde et
            token = authHeader.substring(7);

            // 4. Adım: Token içinden kullanıcının e-posta/kullanıcı adını çözümle
            userName = jwtService.getUsernameByToken(token);

            // 5. Adım: Kullanıcı adı varsa ve o anki istek bağlamında henüz oturum açılmamışsa kontrolleri başlat
            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Veritabanından kullanıcının güncel rollerini ve şifre bilgilerini çek
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

                // 6. Adım: Token süresinin dolup dolmadığını denetle
                if (!jwtService.isTokenExpired(token)) {

                    // Token geçerli ise Spring Security için kimlik doğrulama nesnesi (Authentication) üret
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(userDetails);

                    // 7. Adım: Kimlik doğrulandı bilgisini Spring Security Context'ine kaydet
                    // Artık Controller sınıfları veya @PreAuthorize bu kullanıcıyı tanır
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token'ın kullanım süresi dolmuşsa yakalanır
            System.out.println("Token suresi dolmuştur: " + e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            // Token değiştirilmiş veya imza anahtarı uyuşmuyorsa yakalanır
            System.out.println("Token imzası geçersiz : " + e.getMessage());
        } catch (Exception e) {
            // Token ayrıştırma sırasındaki beklenmeyen diğer tüm hataları yakalar
            System.out.println("Token doğrulanırken  bir hata oluştu: " + e.getMessage());
        }

        // 8. Adım: Kontroller bittikten sonra isteğin bir sonraki güvenlik filtresine ya da Controller'a akmasına izin ver
        filterChain.doFilter(request, response);
    }
}
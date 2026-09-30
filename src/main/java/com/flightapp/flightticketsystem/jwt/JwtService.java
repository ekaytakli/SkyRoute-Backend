package com.flightapp.flightticketsystem.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT (JSON Web Token) üretimi, şifrelenmesi, doğrulanması ve
 * içerisindeki kullanıcı/rol bilgilerinin çözümlenmesini yöneten servis sınıfıdır.
 */
@Service // Spring'in bu sınıfı servis katmanında bir Bean olarak yönetmesini sağlar.
public class JwtService {

    // Token'ları imzalamak ve çözmek için kullanılan 256-bit gizli anahtar.
    // application.properties içinde 'jwt.secret' yoksa iki nokta sonrasındaki varsayılan değeri kullanır.
    @Value("${jwt.secret:Uy9uABa63lWt2sSvqJjqc9qfBV2qfKIFedRRddyoFl2}")
    private String SECRET_KEY;

    /**
     * Doğrulanmış kullanıcı için rol ve süre bilgilerini içeren yeni bir JWT token üretir.
     *
     * @param userDetails Spring Security kullanıcı detay nesnesi
     * @return İmzalanmış ve kompakt hale getirilmiş JWT metni
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claimsMap = new HashMap<>();

        // Kullanıcının yetkilerini (rollerini) liste halinde topla (Örn: ["ROLE_ADMIN", "ROLE_CUSTOMER"])
        java.util.List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList();

        // Rolleri token'ın özel veri yükü (claims) alanına ekle
        claimsMap.put("roles", roles);

        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // Token'ın kime ait olduğunu (e-posta/kullanıcı adı) belirler
                .addClaims(claimsMap) // Eklenen rolleri token içine gömer
                .setIssuedAt(new Date()) // Token'ın oluşturulma zamanını atar
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 2))) // 2 saat geçerlilik süresi belirler
                .signWith(getKey(), SignatureAlgorithm.HS256) // HMAC-SHA256 algoritması ve gizli anahtarla imzalar
                .compact(); // Tüm parçaları tek parça Base64 metnine dönüştürür
    }

    /**
     * Token'ın imzasını gizli anahtarla doğrulayarak içindeki veri gövdesini (Claims) çözer.
     *
     * @param token Çözümlenecek JWT metni
     * @return Token içindeki tüm iddiaları/verileri barındıran Claims nesnesi
     */
    public Claims getClaims(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getKey()) // İmzanın doğrulanacağı gizli anahtarı tanıtır
                .build()
                .parseClaimsJws(token).getBody(); // İmzayı doğrular ve veri gövdesini alır
        return claims;
    }

    /**
     * Token içerisindeki özel bir veriyi (örneğin 'roles') anahtar adıyla çeker.
     *
     * @param token JWT metni
     * @param key   Okunmak istenen claim anahtar adı
     * @return İlgili anahtarın değeri
     */
    public Object getRolesByClaims(String token, String key) {
        Claims claims = getClaims(token);
        Object object = claims.get(key);
        return object;
    }

    /**
     * Fonksiyonel programlama yaklaşımıyla token içinden istenen herhangi bir
     * bilginin (subject, expiration, vb.) dinamik olarak dışarı aktarılmasını sağlar.
     *
     * @param token          JWT metni
     * @param claimsFunction Claims nesnesinden veriyi çekecek fonksiyonel kural
     * @param <T>            Dönecek verinin türü (String, Date vb.)
     * @return İlgili claim değeri
     */
    public <T> T exportToken(String token, Function<Claims, T> claimsFunction) {
        Claims claims = getClaims(token);
        return claimsFunction.apply(claims);
    }

    /**
     * Token'ın kime ait olduğunu (Subject / Kullanıcı Adı) okur.
     *
     * @param token JWT metni
     * @return Kullanıcı adı (e-posta)
     */
    public String getUsernameByToken(String token) {
        return exportToken(token, Claims::getSubject);
    }

    /**
     * Token'ın geçerlilik süresinin dolup dolmadığını kontrol eder.
     *
     * @param token JWT metni
     * @return Süre dolmuşsa true, geçerli ise false döner
     */
    public boolean isTokenExpired(String token) {
        Date expiredDate = exportToken(token, Claims::getExpiration);
        return new Date().after(expiredDate); // Şu anki saat, son geçerlilik tarihinden sonraysa süresi dolmuştur
    }

    /**
     * String formatındaki gizli anahtarı Base64 ile çözüp
     * HMAC-SHA algoritmasına uygun bir şifreleme anahtarına dönüştürür.
     *
     * @return Kriptografik anahtar nesnesi
     */
    public Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
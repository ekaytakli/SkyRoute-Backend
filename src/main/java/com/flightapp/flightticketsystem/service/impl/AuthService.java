package com.flightapp.flightticketsystem.service.impl;

import com.flightapp.flightticketsystem.entities.User;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.jwt.LoginRequest;
import com.flightapp.flightticketsystem.jwt.RegisterRequest;
import com.flightapp.flightticketsystem.jwt.AuthResponse;
import com.flightapp.flightticketsystem.jwt.JwtService;

import com.flightapp.flightticketsystem.repository.RoleRepository;
import com.flightapp.flightticketsystem.repository.UserRepository;
import com.flightapp.flightticketsystem.service.IAuthService;
import com.flightapp.flightticketsystem.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Spring Boot'a bu sınıfın iş mantığı (business logic) katmanında olduğunu belirtir.
@Service
// Lombok: 'final' olarak işaretlenmiş tüm alanlar (dependencies) için otomatik constructor oluşturur (Constructor Injection).
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    // Kullanıcı veritabanı işlemlerini yürüten repository bağımlılığı
    private final UserRepository usersRepository;

    // Rol tablosu sorgulama işlemlerini yapan repository bağımlılığı
    private final RoleRepository rolesRepository;

    // Kullanıcı şifrelerini tek yönlü hashlemek (BCrypt vb.) için kullanılan bileşen
    private final PasswordEncoder passwordEncoder;

    // JWT oluşturma ve doğrulama süreçlerini yöneten servis
    private final JwtService jwtService;

    // Spring Security'nin kimlik doğrulama mekanizmasını tetikleyen temel yönetici
    private final AuthenticationManager authenticationManager;

    // DTO nesneleri ile Entity modelleri arasında alan eşlemesi yapan mapper
    private final UserMapper userMapper;

    /**
     * Yeni bir kullanıcının sisteme kaydını gerçekleştiren metot.
     * @param request Kayıt için gerekli e-posta, şifre ve kişisel bilgileri içeren DTO
     * @return Sisteme hemen giriş yapılabilmesi için üretilen JWT token nesnesi
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        // 1. Adım: Aynı e-posta adresiyle sistemde kayıtlı başka bir kullanıcı var mı kontrol et.
        if (usersRepository.existsByEmail(request.getEmail())) {
            // E-posta zaten varsa işlemi durdur ve iş kuralı hatası fırlat.
            throw new BaseException("error.user_already_exists");
        }

        // 2. Adım: Gelen DTO verisini veritabanı entity modeline (User) çevir.
        User user = userMapper.toEntity(request);

        // 3. Adım: Kullanıcının girdiği açık metin şifreyi kriptolayıp hashlenmiş halini kaydet.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. Adım: Yeni kaydolan her kullanıcıya varsayılan olarak "Customer" rolü ata (varsa listeye ekle).
        rolesRepository.findByRoleName("Customer").ifPresent(role -> user.getRoles().add(role));

        // 5. Adım: Hazırlanan kullanıcı nesnesini veritabanına kaydet.
        usersRepository.save(user);

        // 6. Adım: Kullanıcı için oturum sağlayacak bir JWT token üret.
        String jwtToken = jwtService.generateToken(user);

        // 7. Adım: Token'ı AuthResponse içinde istemciye dön.
        return new AuthResponse(jwtToken);
    }

    /**
     * Mevcut bir kullanıcının sisteme giriş yapmasını sağlayan metot.
     * @param request Kullanıcının girdiği e-posta ve şifreyi içeren DTO
     * @return Giriş başarılı ise üretilen JWT token nesnesi
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Adım: E-posta ve şifreyi Spring Security'nin AuthenticationManager'ına verip doğrulat.
        // Bilgiler hatalıysa bu satır otomatik olarak AuthenticationException fırlatır ve kod durur.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Adım: Kimlik doğrulama başarılı ise kullanıcının tüm bilgilerini veritabanından çek.
        User user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BaseException("error.user_not_found"));

        // 3. Adım: Kullanıcıya özel JWT token oluştur.
        String jwtToken = jwtService.generateToken(user);

        // 4. Adım: Token nesnesini geri döndür.
        return new AuthResponse(jwtToken);
    }
}
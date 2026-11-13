package com.flightapp.flightticketsystem.config;

import com.flightapp.flightticketsystem.entities.Role;
import com.flightapp.flightticketsystem.entities.User;
import com.flightapp.flightticketsystem.repository.RoleRepository;
import com.flightapp.flightticketsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * Uygulama her ayağa kalktığında veritabanında bulunması ZORUNLU olan
 * temel verileri (Roller ve Varsayılan Admin Kullanıcısı) otomatik yükleyen sınıftır.
 */
@Component // Spring'in bu sınıfı otomatik olarak fark edip bir Spring Bean'i olarak yönetmesini sağlar.
@RequiredArgsConstructor // 'final' olarak işaretlenmiş alanlar için Constructor Injection oluşturur.
public class DatabaseSeeder implements CommandLineRunner {

    // Rol tablosu sorgulama ve kaydetme işlemleri için repository
    private final RoleRepository roleRepository;

    // Kullanıcı tablosu sorgulama ve kaydetme işlemleri için repository
    private final UserRepository userRepository;

    // Varsayılan admin şifresini hashleyerek güvenli kaydetmek için şifreleyici
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Boot uygulaması tamamen ayağa kalktığı anda otomatik olarak tetiklenen metottur.
     */
    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean adminEnabled;
    @Value("${app.bootstrap-admin.email:}")
    private String adminEmail;
    @Value("${app.bootstrap-admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        // 1. ADIM: "Admin" rolü veritabanında kayıtlı değilse oluştur ve kaydet.
        if (roleRepository.findByRoleName("Admin").isEmpty()) {
            Role admin = new Role();
            admin.setRoleName("Admin");
            roleRepository.save(admin);
            System.out.println("Admin rolü başarıyla eklendi");
        }

        // 2. ADIM: "Customer" rolü veritabanında kayıtlı değilse oluştur ve kaydet.
        if (roleRepository.findByRoleName("Customer").isEmpty()) {
            Role customer = new Role();
            customer.setRoleName("Customer");
            roleRepository.save(customer);
            System.out.println("Customer rolü başarıyla eklendi");
        }

        // 3. ADIM: Sisteme ilk girişi yapabilmek için varsayılan bir yönetici (Admin) hesabı var mı kontrol et.
        if (!adminEnabled) {
            return;
        }
        if (adminEmail.isBlank() || adminPassword.length() < 12) {
            throw new IllegalStateException("Bootstrap admin requires an email and a password of at least 12 characters");
        }
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            // Yonetici sifresi ortamdan alinir ve hashlenir.
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setFirstName("Sistem");
            adminUser.setLastName("Yöneticisi");

            // Az önce oluşturulan "Admin" rolünü bulup bu kullanıcıya bağla.
            roleRepository.findByRoleName("Admin").ifPresent(role -> adminUser.getRoles().add(role));

            // Hazırlanan ilk admin kullanıcısını veritabanına kaydet.
            userRepository.save(adminUser);
            System.out.println("Yönetici hesabı oluşturuldu.");
        }
    }
}
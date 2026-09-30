package com.flightapp.flightticketsystem.config;

import com.flightapp.flightticketsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security'nin kimlik doğrulama (Authentication) altyapısını yapılandıran
 * temel konfigürasyon sınıfıdır.
 *
 * Bu sınıf; kullanıcı bilgilerinin nereden çekileceğini, şifrelerin nasıl çözüleceğini
 * ve kimlik kontrol mekanizmalarını Spring Context'e Bean olarak tanıtır.
 */
@Configuration // Spring'e bu sınıfın uygulama başlangıcında çalıştırılacak konfigürasyon ve Bean tanımları içerdiğini bildirir.
@RequiredArgsConstructor // 'final' tanımlanmış bağımlılıklar (UserRepository) için otomatik constructor oluşturarak Dependency Injection sağlar.
public class AppConfig {

    // Kullanıcı sorgulamalarını veritabanında yapmak için kullanılan veri erişim katmanı arayüzü.
    private final UserRepository usersRepository;

    /**
     * Spring Security'nin kullanıcıyı veritabanından nasıl bulacağını tanımlayan servistir.
     *
     * Spring Security varsayılan olarak kullanıcı adı (username) bekler; ancak bu sistemde
     * girişler e-posta üzerinden yapıldığı için username parametresi kullanıcı e-postası olarak kabul edilir.
     *
     * @return Lambda ifadesi ile UserDetailsService arayüzünü uygulayan fonksiyon.
     * @throws UsernameNotFoundException Kullanıcı veritabanında bulunamazsa Spring Security'nin standart istisnasını fırlatır.
     */
    @Bean // Üretilen nesnenin Spring IoC konteyneri tarafından yönetilen bir Bean olmasını sağlar.
    public UserDetailsService userDetailsService() {
        return username -> usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Kimlik doğrulama sağlayıcısını (AuthenticationProvider) yapılandırır.
     *
     * DaoAuthenticationProvider, kullanıcı detaylarını (UserDetailsService) ve şifreleme algoritmasını
     * (PasswordEncoder) bir araya getirerek kullanıcının girdiği şifre ile veritabanındaki hashlenmiş şifreyi karşılaştırır.
     *
     * @return Yapılandırılmış DaoAuthenticationProvider nesnesi.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Veritabanı tabanlı kimlik doğrulama sağlayıcısı oluşturulur.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Kullanıcı verilerini getirecek olan servis tanıtılır.
        authProvider.setUserDetailsService(userDetailsService());

        // Şifreleri doğrulamak için kullanılacak hash çözücü/karşılaştırıcı algoritma tanıtılır.
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Kimlik doğrulama sürecini yöneten ana yönetici bileşeni (AuthenticationManager) Spring konteynerine kaydeder.
     *
     * Bu bileşen; AuthService gibi yerlerde kullanıcının e-posta ve şifresini kontrol etmek (authenticate etmek) için doğrudan çağrılır.
     *
     * @param config Spring Security'nin hazır kimlik doğrulama yapılandırma nesnesi.
     * @return Uygulama genelinde kullanılacak AuthenticationManager Bean'i.
     * @throws Exception Yönetici nesnesi alınırken oluşabilecek hatalar için.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Şifreleri açık metin (plain text) olarak saklamayıp güvenli bir şekilde tek yönlü
     * hashlemek için kullanılan şifreleme algoritmasını tanımlar.
     *
     * Sistem genelinde endüstri standardı olan BCrypt algoritması kullanılır.
     *
     * @return BCryptPasswordEncoder nesnesi.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
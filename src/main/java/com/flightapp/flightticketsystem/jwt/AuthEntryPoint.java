package com.flightapp.flightticketsystem.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Kimliği doğrulanmamış (unauthenticated) veya geçersiz/eksik JWT token ile
 * korumalı bir kaynağa erişmeye çalışan istekler yakalandığında devreye giren bileşendir.
 *
 * Spring Security'nin AuthenticationEntryPoint arayüzünü uygulayarak
 * 401 Unauthorized HTTP yanıtının standart şekilde dönmesini sağlar.
 */
@Component // Spring IoC konteyneri tarafından yönetilen genel bir bileşen (Bean) olduğunu belirtir.
public class AuthEntryPoint implements AuthenticationEntryPoint {

    /**
     * Kimlik doğrulama başarısız olduğunda veya kullanıcı hiç kimlik bilgisi (Token)
     * sunmadan korunan bir endpoint'e erişmeye çalıştığında otomatik olarak tetiklenir.
     *
     * @param request       Yetkisiz erişimin yapıldığı HTTP istek nesnesi.
     * @param response      İstemciye geri gönderilecek HTTP yanıt nesnesi.
     * @param authException Kimlik doğrulama sürecinde fırlatılan temel güvenlik istisnası.
     * @throws IOException      Ağ/yanıt yazma hatası oluşursa fırlatılır.
     * @throws ServletException Servlet seviyesinde bir aksaklık oluşursa fırlatılır.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // İstemciye HTTP 401 durum kodu (HttpServletResponse.SC_UNAUTHORIZED) ve "Unauthorized" mesajını iletir.
        // Bu sayede frontend (React, Angular, mobil vb.) kullanıcının oturumunun olmadığını anlar ve login sayfasına yönlendirir.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
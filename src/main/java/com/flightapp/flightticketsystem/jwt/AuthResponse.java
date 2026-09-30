package com.flightapp.flightticketsystem.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Başarılı kimlik doğrulama (Giriş/Login veya Kayıt/Register) işlemlerinin ardından
 * istemciye (Frontend) döndürülen yanıt (Response) modelidir.
 *
 * Sunucu tarafından üretilen JWT token'ı güvenli şekilde taşımakla görevlidir.
 */
@Data // Lombok: Getter, Setter, toString, equals ve hashCode metotlarını otomatik üretir.
@AllArgsConstructor // Lombok: Tüm alanları içeren yapıcı metodu (Constructor) oluşturur.
@NoArgsConstructor // Lombok: Parametresiz varsayılan yapıcı metodu oluşturur.
public class AuthResponse {

    /**
     * Kullanıcının kimliğini ve yetkilerini temsil eden, sunucu tarafından imzalanmış
     * Base64 formatındaki JSON Web Token (JWT) dizesidir.
     *
     * İstemci bu anahtarı tarayıcı hafızasında saklar ve korumalı isteklere
     * "Authorization: Bearer <token>" başlığı altında ekleyerek gönderir.
     */
    private String token;
}
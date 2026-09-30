package com.flightapp.flightticketsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcı bilgilerini dış dünyaya açarken kullanılan DTO sınıfı.
 * User Entity'si içindeki 'password' gibi hassas verileri gizleyerek
 * sadece istemcinin görmesi gereken temel profil bilgilerini taşır.
 */
@Data // Lombok: Getter, Setter, toString, equalsAndHashCode metotlarını otomatik üretir.
@NoArgsConstructor // Lombok: Parametresiz varsayılan yapıcı metot oluşturur.
@AllArgsConstructor // Lombok: Tüm alanları içeren yapıcı metot oluşturur.
public class DtoUsers {
    // Kullanıcının benzersiz ID numarası
    private Integer id;

    // Kullanıcının sistemde kayıtlı e-posta adresi
    private String email;

    // Kullanıcının adı
    private String firstName;

    // Kullanıcının soyadı
    private String lastName;
}